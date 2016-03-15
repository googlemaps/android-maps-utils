package de.lighti.clipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lighti.clipper.Clipper.ClipType;
import de.lighti.clipper.Clipper.EndType;
import de.lighti.clipper.Clipper.JoinType;
import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.Clipper.PolyType;
import de.lighti.clipper.Point.DoublePoint;
import de.lighti.clipper.Point.LongPoint;

public class ClipperOffset {
    private static boolean nearZero( double val ) {
        return val > -TOLERANCE && val < TOLERANCE;
    }

    private Paths destPolys;
    private Path srcPoly;
    private Path destPoly;

    private final List<DoublePoint> normals;
    private double delta, inA, sin, cos;

    private double miterLim, stepsPerRad;
    private LongPoint lowest;

    private final PolyNode polyNodes;
    private final double arcTolerance;

    private final double miterLimit;
    private final static double TWO_PI = Math.PI * 2;

    private final static double DEFAULT_ARC_TOLERANCE = 0.25;

    private final static double TOLERANCE = 1.0E-20;

    public ClipperOffset() {
        this( 2, DEFAULT_ARC_TOLERANCE );
    }

    public ClipperOffset( double miterLimit, double arcTolerance ) {
        this.miterLimit = miterLimit;
        this.arcTolerance = arcTolerance;
        lowest = new LongPoint();
        lowest.setX( -1l );
        polyNodes = new PolyNode();
        normals = new ArrayList<DoublePoint>();
    }

    public void addPath( Path path, JoinType joinType, EndType endType ) {
        int highI = path.size() - 1;
        if (highI < 0) {
            return;
        }
        final PolyNode newNode = new PolyNode();
        newNode.setJoinType( joinType );
        newNode.setEndType( endType );

        //strip duplicate points from path and also get index to the lowest point ...
        if (endType == EndType.CLOSED_LINE || endType == EndType.CLOSED_POLYGON) {
            while (highI > 0 && path.get( 0 ) == path.get( highI )) {
                highI--;
            }
        }

        newNode.getPolygon().add( path.get( 0 ) );
        int j = 0, k = 0;
        for (int i = 1; i <= highI; i++) {
            if (newNode.getPolygon().get( j ) != path.get( i )) {
                j++;
                newNode.getPolygon().add( path.get( i ) );
                if (path.get( i ).getY() > newNode.getPolygon().get( k ).getY() || path.get( i ).getY() == newNode.getPolygon().get( k ).getY()
                                && path.get( i ).getX() < newNode.getPolygon().get( k ).getX()) {
                    k = j;
                }
            }
        }
        if (endType == EndType.CLOSED_POLYGON && j < 2) {
            return;
        }

        polyNodes.addChild( newNode );

        //if this path's lowest pt is lower than all the others then update m_lowest
        if (endType != EndType.CLOSED_POLYGON) {
            return;
        }
        if (lowest.getX() < 0) {
            lowest = new LongPoint( polyNodes.getChildCount() - 1, k );
        }
        else {
            final LongPoint ip = polyNodes.getChilds().get( (int) lowest.getX() ).getPolygon().get( (int) lowest.getY() );
            if (newNode.getPolygon().get( k ).getY() > ip.getY() || newNode.getPolygon().get( k ).getY() == ip.getY()
                            && newNode.getPolygon().get( k ).getX() < ip.getX()) {
                lowest = new LongPoint( polyNodes.getChildCount() - 1, k );
            }
        }
    }

    public void addPaths( Paths paths, JoinType joinType, EndType endType ) {
        for (final Path p : paths) {
            addPath( p, joinType, endType );
        }
    }

    public void clear() {
        polyNodes.getChilds().clear();
        lowest.setX( -1l );
    }

    private void doMiter( int j, int k, double r ) {
        final double q = delta / r;
        destPoly.add( new LongPoint( (int) Math.round( srcPoly.get( j ).getX() + (normals.get( k ).getX() + normals.get( j ).getX()) * q ), (int) Math
                        .round( srcPoly.get( j ).getY() + (normals.get( k ).getY() + normals.get( j ).getY()) * q ) ) );
    }

    private void doOffset( double delta ) {
        destPolys = new Paths();
        this.delta = delta;

        //if Zero offset, just copy any CLOSED polygons to m_p and return ...
        if (nearZero( delta )) {
            for (int i = 0; i < polyNodes.getChildCount(); i++) {
                final PolyNode node = polyNodes.getChilds().get( i );
                if (node.getEndType() == EndType.CLOSED_POLYGON) {
                    destPolys.add( node.getPolygon() );
                }
            }
            return;
        }

        //see offset_triginometry3.svg in the documentation folder ...
        if (miterLimit > 2) {
            miterLim = 2 / (miterLimit * miterLimit);
        }
        else {
            miterLim = 0.5;
        }

        double y;
        if (arcTolerance <= 0.0) {
            y = DEFAULT_ARC_TOLERANCE;
        }
        else if (arcTolerance > Math.abs( delta ) * DEFAULT_ARC_TOLERANCE) {
            y = Math.abs( delta ) * DEFAULT_ARC_TOLERANCE;
        }
        else {
            y = arcTolerance;
        }
        //see offset_triginometry2.svg in the documentation folder ...
        final double steps = Math.PI / Math.acos( 1 - y / Math.abs( delta ) );
        sin = Math.sin( TWO_PI / steps );
        cos = Math.cos( TWO_PI / steps );
        stepsPerRad = steps / TWO_PI;
        if (delta < 0.0) {
            sin = -sin;
        }

        for (int i = 0; i < polyNodes.getChildCount(); i++) {
            final PolyNode node = polyNodes.getChilds().get( i );
            srcPoly = node.getPolygon();

            final int len = srcPoly.size();

            if (len == 0 || delta <= 0 && (len < 3 || node.getEndType() != EndType.CLOSED_POLYGON)) {
                continue;
            }

            destPoly = new Path();

            if (len == 1) {
                if (node.getJoinType() == JoinType.ROUND) {
                    double X = 1.0, Y = 0.0;
                    for (int j = 1; j <= steps; j++) {
                        destPoly.add( new LongPoint( (int) Math.round( srcPoly.get( 0 ).getX() + X * delta ), (int) Math.round( srcPoly.get( 0 ).getY() + Y
                                        * delta ) ) );
                        final double X2 = X;
                        X = X * cos - sin * Y;
                        Y = X2 * sin + Y * cos;
                    }
                }
                else {
                    double X = -1.0, Y = -1.0;
                    for (int j = 0; j < 4; ++j) {
                        destPoly.add( new LongPoint( (int) Math.round( srcPoly.get( 0 ).getX() + X * delta ), (int) Math.round( srcPoly.get( 0 ).getY() + Y
                                        * delta ) ) );
                        if (X < 0) {
                            X = 1;
                        }
                        else if (Y < 0) {
                            Y = 1;
                        }
                        else {
                            X = -1;
                        }
                    }
                }
                destPolys.add( destPoly );
                continue;
            }

            //build m_normals ...
            normals.clear();
            for (int j = 0; j < len - 1; j++) {
                normals.add( Point.getUnitNormal( srcPoly.get( j ), srcPoly.get( j + 1 ) ) );
            }
            if (node.getEndType() == EndType.CLOSED_LINE || node.getEndType() == EndType.CLOSED_POLYGON) {
                normals.add( Point.getUnitNormal( srcPoly.get( len - 1 ), srcPoly.get( 0 ) ) );
            }
            else {
                normals.add( new DoublePoint( normals.get( len - 2 ) ) );
            }

            if (node.getEndType() == EndType.CLOSED_POLYGON) {
                final int[] k = new int[] { len - 1 };
                for (int j = 0; j < len; j++) {
                    offsetPoint( j, k, node.getJoinType() );
                }
                destPolys.add( destPoly );
            }
            else if (node.getEndType() == EndType.CLOSED_LINE) {
                final int[] k = new int[] { len - 1 };
                for (int j = 0; j < len; j++) {
                    offsetPoint( j, k, node.getJoinType() );
                }
                destPolys.add( destPoly );
                destPoly = new Path();
                //re-build m_normals ...
                final DoublePoint n = normals.get( len - 1 );
                for (int j = len - 1; j > 0; j--) {
                    normals.set( j, new DoublePoint( -normals.get( j - 1 ).getX(), -normals.get( j - 1 ).getY() ) );
                }
                normals.set( 0, new DoublePoint( -n.getX(), -n.getY(), 0 ) );
                k[0] = 0;
                for (int j = len - 1; j >= 0; j--) {
                    offsetPoint( j, k, node.getJoinType() );
                }
                destPolys.add( destPoly );
            }
            else {
                final int[] k = new int[1];
                for (int j = 1; j < len - 1; ++j) {
                    offsetPoint( j, k, node.getJoinType() );
                }

                LongPoint pt1;
                if (node.getEndType() == EndType.OPEN_BUTT) {
                    final int j = len - 1;
                    pt1 = new LongPoint( (int) Math.round( srcPoly.get( j ).getX() + normals.get( j ).getX() * delta ), (int) Math.round( srcPoly.get( j )
                                    .getY() + normals.get( j ).getY() * delta ), 0 );
                    destPoly.add( pt1 );
                    pt1 = new LongPoint( (int) Math.round( srcPoly.get( j ).getX() - normals.get( j ).getX() * delta ), (int) Math.round( srcPoly.get( j )
                                    .getY() - normals.get( j ).getY() * delta ), 0 );
                    destPoly.add( pt1 );
                }
                else {
                    final int j = len - 1;
                    k[0] = len - 2;
                    inA = 0;
                    normals.set( j, new DoublePoint( -normals.get( j ).getX(), -normals.get( j ).getY() ) );
                    if (node.getEndType() == EndType.OPEN_SQUARE) {
                        doSquare( j, k[0] );
                    }
                    else {
                        doRound( j, k[0] );
                    }
                }

                //re-build m_normals ...
                for (int j = len - 1; j > 0; j--) {
                    normals.set( j, new DoublePoint( -normals.get( j - 1 ).getX(), -normals.get( j - 1 ).getY() ) );
                }

                normals.set( 0, new DoublePoint( -normals.get( 1 ).getX(), -normals.get( 1 ).getY() ) );

                k[0] = len - 1;
                for (int j = k[0] - 1; j > 0; --j) {
                    offsetPoint( j, k, node.getJoinType() );
                }

                if (node.getEndType() == EndType.OPEN_BUTT) {
                    pt1 = new LongPoint( (int) Math.round( srcPoly.get( 0 ).getX() - normals.get( 0 ).getX() * delta ), (int) Math.round( srcPoly.get( 0 )
                                    .getY() - normals.get( 0 ).getY() * delta ) );
                    destPoly.add( pt1 );
                    pt1 = new LongPoint( (int) Math.round( srcPoly.get( 0 ).getX() + normals.get( 0 ).getX() * delta ), (int) Math.round( srcPoly.get( 0 )
                                    .getY() + normals.get( 0 ).getY() * delta ) );
                    destPoly.add( pt1 );
                }
                else {
                    k[0] = 1;
                    inA = 0;
                    if (node.getEndType() == EndType.OPEN_SQUARE) {
                        doSquare( 0, 1 );
                    }
                    else {
                        doRound( 0, 1 );
                    }
                }
                destPolys.add( destPoly );
            }
        }
    }

    private void doRound( int j, int k ) {
        final double a = Math.atan2( inA, normals.get( k ).getX() * normals.get( j ).getX() + normals.get( k ).getY() * normals.get( j ).getY() );
        final int steps = Math.max( (int) Math.round( stepsPerRad * Math.abs( a ) ), 1 );

        double X = normals.get( k ).getX(), Y = normals.get( k ).getY(), X2;
        for (int i = 0; i < steps; ++i) {
            destPoly.add( new LongPoint( (int) Math.round( srcPoly.get( j ).getX() + X * delta ), (int) Math.round( srcPoly.get( j ).getY() + Y * delta ) ) );
            X2 = X;
            X = X * cos - sin * Y;
            Y = X2 * sin + Y * cos;
        }
        destPoly.add( new LongPoint( (int) Math.round( srcPoly.get( j ).getX() + normals.get( j ).getX() * delta ), (int) Math.round( srcPoly.get( j ).getY()
                        + normals.get( j ).getY() * delta ) ) );
    }

    private void doSquare( int j, int k ) {
        final double nkx = normals.get( k ).getX();
        final double nky = normals.get( k ).getY();
        final double njx = normals.get( j ).getX();
        final double njy = normals.get( j ).getY();
        final double sjx = srcPoly.get( j ).getX();
        final double sjy = srcPoly.get( j ).getY();
        final double dx = Math.tan( Math.atan2( inA, nkx * njx + nky * njy ) / 4 );
        destPoly.add( new LongPoint( (int) Math.round( sjx + delta * (nkx - nky * dx) ), (int) Math.round( sjy + delta * (nky + nkx * dx) ), 0 ) );
        destPoly.add( new LongPoint( (int) Math.round( sjx + delta * (njx + njy * dx) ), (int) Math.round( sjy + delta * (njy - njx * dx) ), 0 ) );
    }

    //------------------------------------------------------------------------------

    public void execute( Paths solution, double delta ) {
        solution.clear();
        fixOrientations();
        doOffset( delta );
        //now clean up 'corners' ...
        final DefaultClipper clpr = new DefaultClipper( Clipper.REVERSE_SOLUTION );
        clpr.addPaths( destPolys, PolyType.SUBJECT, true );
        if (delta > 0) {
            clpr.execute( ClipType.UNION, solution, PolyFillType.POSITIVE, PolyFillType.POSITIVE );
        }
        else {
            final LongRect r = destPolys.getBounds();
            final Path outer = new Path( 4 );

            outer.add( new LongPoint( r.left - 10, r.bottom + 10, 0 ) );
            outer.add( new LongPoint( r.right + 10, r.bottom + 10, 0 ) );
            outer.add( new LongPoint( r.right + 10, r.top - 10, 0 ) );
            outer.add( new LongPoint( r.left - 10, r.top - 10, 0 ) );

            clpr.addPath( outer, PolyType.SUBJECT, true );

            clpr.execute( ClipType.UNION, solution, PolyFillType.NEGATIVE, PolyFillType.NEGATIVE );
            if (solution.size() > 0) {
                solution.remove( 0 );
            }
        }
    }

    //------------------------------------------------------------------------------

    public void execute( PolyTree solution, double delta ) {
        solution.Clear();
        fixOrientations();
        doOffset( delta );

        //now clean up 'corners' ...
        final DefaultClipper clpr = new DefaultClipper( Clipper.REVERSE_SOLUTION );
        clpr.addPaths( destPolys, PolyType.SUBJECT, true );
        if (delta > 0) {
            clpr.execute( ClipType.UNION, solution, PolyFillType.POSITIVE, PolyFillType.POSITIVE );
        }
        else {
            final LongRect r = destPolys.getBounds();
            final Path outer = new Path( 4 );

            outer.add( new LongPoint( r.left - 10, r.bottom + 10, 0 ) );
            outer.add( new LongPoint( r.right + 10, r.bottom + 10, 0 ) );
            outer.add( new LongPoint( r.right + 10, r.top - 10, 0 ) );
            outer.add( new LongPoint( r.left - 10, r.top - 10, 0 ) );

            clpr.addPath( outer, PolyType.SUBJECT, true );

            clpr.execute( ClipType.UNION, solution, PolyFillType.NEGATIVE, PolyFillType.NEGATIVE );
            //remove the outer PolyNode rectangle ...
            if (solution.getChildCount() == 1 && solution.getChilds().get( 0 ).getChildCount() > 0) {
                final PolyNode outerNode = solution.getChilds().get( 0 );
                solution.getChilds().set( 0, outerNode.getChilds().get( 0 ) );
                solution.getChilds().get( 0 ).setParent( solution );
                for (int i = 1; i < outerNode.getChildCount(); i++) {
                    solution.addChild( outerNode.getChilds().get( i ) );
                }
            }
            else {
                solution.Clear();
            }
        }
    }

    //------------------------------------------------------------------------------

    private void fixOrientations() {
        //fixup orientations of all closed paths if the orientation of the
        //closed path with the lowermost vertex is wrong ...
        if (lowest.getX() >= 0 && !polyNodes.childs.get( (int) lowest.getX() ).getPolygon().orientation()) {
            for (int i = 0; i < polyNodes.getChildCount(); i++) {
                final PolyNode node = polyNodes.childs.get( i );
                if (node.getEndType() == EndType.CLOSED_POLYGON || node.getEndType() == EndType.CLOSED_LINE && node.getPolygon().orientation()) {
                    Collections.reverse( node.getPolygon() );

                }
            }
        }
        else {
            for (int i = 0; i < polyNodes.getChildCount(); i++) {
                final PolyNode node = polyNodes.childs.get( i );
                if (node.getEndType() == EndType.CLOSED_LINE && !node.getPolygon().orientation()) {
                    Collections.reverse( node.getPolygon() );
                }
            }
        }
    }

    private void offsetPoint( int j, int[] kV, JoinType jointype ) {
        //cross product ...
        final int k = kV[0];
        final double nkx = normals.get( k ).getX();
        final double nky = normals.get( k ).getY();
        final double njy = normals.get( j ).getY();
        final double njx = normals.get( j ).getX();
        final long sjx = srcPoly.get( j ).getX();
        final long sjy = srcPoly.get( j ).getY();
        inA = nkx * njy - njx * nky;

        if (Math.abs( inA * delta ) < 1.0) {
            //dot product ...

            final double cosA = nkx * njx + njy * nky;
            if (cosA > 0) // angle ==> 0 degrees
            {
                destPoly.add( new LongPoint( (int) Math.round( sjx + nkx * delta ), (int) Math.round( sjy + nky * delta ), 0 ) );
                return;
            }
            //else angle ==> 180 degrees
        }
        else if (inA > 1.0) {
            inA = 1.0;
        }
        else if (inA < -1.0) {
            inA = -1.0;
        }

        if (inA * delta < 0) {
            destPoly.add( new LongPoint( (int) Math.round( sjx + nkx * delta ), (int) Math.round( sjy + nky * delta ) ) );
            destPoly.add( srcPoly.get( j ) );
            destPoly.add( new LongPoint( (int) Math.round( sjx + njx * delta ), (int) Math.round( sjy + njy * delta ) ) );
        }
        else {
            switch (jointype) {
                case MITER: {
                    final double r = 1 + njx * nkx + njy * nky;
                    if (r >= miterLim) {
                        doMiter( j, k, r );
                    }
                    else {
                        doSquare( j, k );
                    }
                    break;
                }
                case SQUARE:
                    doSquare( j, k );
                    break;
                case ROUND:
                    doRound( j, k );
                    break;
            }
        }
        kV[0] = j;
    }
    //------------------------------------------------------------------------------
}