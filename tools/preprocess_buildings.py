#!/usr/bin/env python3
"""
Preprocesses a large CSV of latitude/longitude coordinates into a compact,
memory-mappable hierarchical spatial pyramid (.scbin) for Android Google Maps clustering.

Uses greedy radius clustering with spatial hash acceleration to enforce minimum
inter-cluster distance, preventing cluster badge overlap on screen.
"""

import argparse
import math
import os
import struct
import sys
import time

def lng_to_x(lng: float) -> float:
    return lng / 360.0 + 0.5

def lat_to_y(lat: float) -> float:
    sin_lat = math.sin(lat * math.pi / 180.0)
    sin_lat = max(-0.9999, min(0.9999, sin_lat))
    y = 0.5 - 0.25 * math.log((1.0 + sin_lat) / (1.0 - sin_lat)) / math.pi
    return max(0.0, min(1.0, y))

def preprocess(csv_path: str, output_path: str, max_cluster_zoom: int = 16, pixel_radius: float = 96.0):
    print(f"Reading CSV from: {csv_path}")
    t0 = time.time()
    
    points = []
    min_lat, max_lat = 90.0, -90.0
    min_lng, max_lng = 180.0, -180.0
    
    with open(csv_path, 'r', encoding='utf-8') as f:
        header = next(f, None)
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = line.split(',')
            lat, lng = float(parts[0]), float(parts[1])
            points.append((lat, lng))
            if lat < min_lat: min_lat = lat
            if lat > max_lat: max_lat = lat
            if lng < min_lng: min_lng = lng
            if lng > max_lng: max_lng = lng

    total_points = len(points)
    print(f"Loaded {total_points:,} points in {time.time() - t0:.2f}s")
    print(f"Bounds: Lat [{min_lat:.6f}, {max_lat:.6f}], Lng [{min_lng:.6f}, {max_lng:.6f}]")
    print(f"Cluster pixel radius: {pixel_radius}px")

    # Initial points: (lat, lng, count, mercator_x, mercator_y)
    curr = []
    for lat, lng in points:
        curr.append((lat, lng, 1, lng_to_x(lng), lat_to_y(lat)))

    t_pyramid = time.time()
    levels = {}

    for z in range(max_cluster_zoom, -1, -1):
        t_z = time.time()
        # Radius in Mercator [0, 1] coordinate space
        r = pixel_radius / (256.0 * (2 ** z))
        r2 = r * r

        # Index current points into a spatial hash grid of cell size r
        grid = {}
        for i, it in enumerate(curr):
            gx = int(it[3] / r)
            gy = int(it[4] / r)
            k = (gx, gy)
            if k not in grid:
                grid[k] = []
            grid[k].append(i)

        clustered = [False] * len(curr)
        next_level = []

        # Greedy radius clustering: any item within distance r is absorbed and marked
        for i in range(len(curr)):
            if clustered[i]:
                continue
            it = curr[i]
            gx = int(it[3] / r)
            gy = int(it[4] / r)

            sum_lat = it[0] * it[2]
            sum_lng = it[1] * it[2]
            cnt = it[2]
            clustered[i] = True

            # Query 9 adjacent cells
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    cell = grid.get((gx + dx, gy + dy))
                    if not cell:
                        continue
                    for nid in cell:
                        if clustered[nid]:
                            continue
                        nit = curr[nid]
                        dist2 = (it[3] - nit[3]) ** 2 + (it[4] - nit[4]) ** 2
                        if dist2 <= r2:
                            clustered[nid] = True
                            sum_lat += nit[0] * nit[2]
                            sum_lng += nit[1] * nit[2]
                            cnt += nit[2]

            c_lat = sum_lat / cnt
            c_lng = sum_lng / cnt
            next_level.append((c_lat, c_lng, cnt, lng_to_x(c_lng), lat_to_y(c_lat)))

        levels[z] = next_level
        print(f"Zoom {z:2d}: {len(next_level):6,d} clusters ({time.time() - t_z:.2f}s)")
        curr = next_level

    print(f"Pyramid generated in {time.time() - t_pyramid:.2f}s")

    # Create destination directory if needed
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)

    print(f"Writing binary pyramid to {output_path}...")
    with open(output_path, 'wb') as f:
        # Magic: 8 bytes
        f.write(b'SCBIN2\x00\x00')
        # Header (32 bytes): totalPoints, minZoom, maxClusterZoom, minLat, maxLat, minLng, maxLng, reserved
        f.write(struct.pack('<iii ffff i', total_points, 0, max_cluster_zoom, min_lat, max_lat, min_lng, max_lng, 0))

        # Placeholder for zoom index table: (count, offset, reserved) per level (16 bytes each)
        table_offset = f.tell()
        f.write(b'\x00' * ((max_cluster_zoom + 1) * 16))

        # Placeholder for raw points table (16 bytes)
        raw_table_offset = f.tell()
        f.write(b'\x00' * 16)

        # Write each zoom level data sorted by latitude
        zoom_table = []
        for z in range(max_cluster_zoom + 1):
            offset = f.tell()
            items = levels[z]
            items.sort(key=lambda it: it[0])  # sort by latitude for binary search
            for lat, lng, count, _, _ in items:
                f.write(struct.pack('<ffi', lat, lng, count))
            zoom_table.append((len(items), offset))

        # Write raw points sorted by latitude for high zoom rendering
        raw_offset = f.tell()
        points.sort(key=lambda it: it[0])
        for lat, lng in points:
            f.write(struct.pack('<ff', lat, lng))
        raw_count = len(points)

        # Fill index tables
        f.seek(table_offset)
        for count, offset in zoom_table:
            f.write(struct.pack('<iqi', count, offset, 0))

        f.seek(raw_table_offset)
        f.write(struct.pack('<iqi', raw_count, raw_offset, 0))

    file_size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(f"Binary file created successfully: {file_size_mb:.2f} MB in {time.time() - t0:.2f}s total.")

def main():
    parser = argparse.ArgumentParser(description="Preprocess coordinate CSV into binary SCBIN2 format with non-overlapping clusters")
    parser.add_argument("--input", required=True, help="Path to input building_lat_lng.csv")
    parser.add_argument("--output", required=True, help="Path to output .scbin file")
    parser.add_argument("--max-cluster-zoom", type=int, default=18, help="Max zoom level to cluster")
    parser.add_argument("--radius", type=float, default=96.0, help="Cluster radius in pixels (default: 96.0)")
    args = parser.parse_args()

    preprocess(args.input, args.output, args.max_cluster_zoom, args.radius)

if __name__ == "__main__":
    main()
