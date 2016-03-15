package de.lighti.clipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import de.lighti.clipper.Path.Join;
import de.lighti.clipper.Path.OutRec;
import de.lighti.clipper.Point.LongPoint;

public class DefaultClipper extends ClipperBase {
    private class IntersectNode {
        Edge edge1;
        Edge Edge2;
        private LongPoint pt;

        public LongPoint getPt() {
            return pt;
        }

        public void setPt( LongPoint pt ) {
            this.pt = pt;
        }

    };

    private static void getHorzDirection( Edge HorzEdge, Direction[] Dir, long[] Left, long[] Right ) {
        if (HorzEdge.getBot().getX() < HorzEdge.getTop().getX()) {
            Left[0] = HorzEdge.getBot().getX();
            Right[0] = HorzEdge.getTop().getX();
            Dir[0] = Direction.LEFT_TO_RIGHT;
        }
        else {
            Left[0] = HorzEdge.getTop().getX();
            Right[0] = HorzEdge.getBot().getX();
            Dir[0] = Direction.RIGHT_TO_LEFT;
        }
    }

    private static boolean getOverlap( long a1, long a2, long b1, long b2, long[] Left, long[] Right ) {
        if (a1 < a2) {
            if (b1 < b2) {
                Left[0] = Math.max( a1, b1 );
                Right[0] = Math.min( a2, b2 );
            }
            else {
                Left[0] = Math.max( a1, b2 );
                Right[0] = Math.min( a2, b1 );
            }
        }
        else {
            if (b1 < b2) {
                Left[0] = Math.max( a2, b1 );
                Right[0] = Math.min( a1, b2 );
            }
            else {
                Left[0] = Math.max( a2, b2 );
                Right[0] = Math.min( a1, b1 );
            }
        }
        return Left[0] < Right[0];
    }

    private static boolean isParam1RightOfParam2( OutRec outRec1, OutRec outRec2 ) {
        do {
            outRec1 = outRec1.firstLeft;
            if (outRec1 == outRec2) {
                return true;
            }
        }
        while (outRec1 != null);
        return false;
    }

    private static int isPointInPolygon( LongPoint pt, Path.OutPt op ) {
        //returns 0 if false, +1 if true, -1 if pt ON polygon boundary
        //See "The Point in Polygon Problem for Arbitrary Polygons" by Hormann & Agathos
        //http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.88.5498&rep=rep1&type=pdf
        int result = 0;
        final Path.OutPt startOp = op;
        final long ptx = pt.getX(), pty = pt.getY();
        long poly0x = op.getPt().getX(), poly0y = op.getPt().getY();
        do {
            op = op.next;
            final long poly1x = op.getPt().getX(), poly1y = op.getPt().getY();

            if (poly1y == pty) {
                if (poly1x == ptx || poly0y == pty && poly1x > ptx == poly0x < ptx) {
                    return -1;
                }
            }
            if (poly0y < pty != poly1y < pty) {
                if (poly0x >= ptx) {
                    if (poly1x > ptx) {
                        result = 1 - result;
                    }
                    else {
                        final double d = (double) (poly0x - ptx) * (poly1y - pty) - (double) (poly1x - ptx) * (poly0y - pty);
                        if (d == 0) {
                            return -1;
                        }
                        if (d > 0 == poly1y > poly0y) {
                            result = 1 - result;
                        }
                    }
                }
                else {
                    if (poly1x > ptx) {
                        final double d = (double) (poly0x - ptx) * (poly1y - pty) - (double) (poly1x - ptx) * (poly0y - pty);
                        if (d == 0) {
                            return -1;
                        }
                        if (d > 0 == poly1y > poly0y) {
                            result = 1 - result;
                        }
                    }
                }
            }
            poly0x = poly1x;
            poly0y = poly1y;
        }
        while (startOp != op);

        return result;
    }

    //------------------------------------------------------------------------------
    private static boolean joinHorz( Path.OutPt op1, Path.OutPt op1b, Path.OutPt op2, Path.OutPt op2b, LongPoint Pt, boolean DiscardLeft ) {
        final Direction Dir1 = op1.getPt().getX() > op1b.getPt().getX() ? Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT;
        final Direction Dir2 = op2.getPt().getX() > op2b.getPt().getX() ? Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT;
        if (Dir1 == Dir2) {
            return false;
        }

        //When DiscardLeft, we want Op1b to be on the Left of Op1, otherwise we
        //want Op1b to be on the Right. (And likewise with Op2 and Op2b.)
        //So, to facilitate this while inserting Op1b and Op2b ...
        //when DiscardLeft, make sure we're AT or RIGHT of Pt before adding Op1b,
        //otherwise make sure we're AT or LEFT of Pt. (Likewise with Op2b.)
        if (Dir1 == Direction.LEFT_TO_RIGHT) {
            while (op1.next.getPt().getX() <= Pt.getX() && op1.next.getPt().getX() >= op1.getPt().getX() && op1.next.getPt().getY() == Pt.getY()) {
                op1 = op1.next;
            }
            if (DiscardLeft && op1.getPt().getX() != Pt.getX()) {
                op1 = op1.next;
            }
            op1b = op1.duplicate( !DiscardLeft );
            if (!op1b.getPt().equals( Pt )) {
                op1 = op1b;
                op1.setPt( new LongPoint( Pt ) );
                op1b = op1.duplicate( !DiscardLeft );
            }
        }
        else {
            while (op1.next.getPt().getX() >= Pt.getX() && op1.next.getPt().getX() <= op1.getPt().getX() && op1.next.getPt().getY() == Pt.getY()) {
                op1 = op1.next;
            }
            if (!DiscardLeft && op1.getPt().getX() != Pt.getX()) {
                op1 = op1.next;
            }
            op1b = op1.duplicate( DiscardLeft );
            if (!op1b.getPt().equals( Pt )) {
                op1 = op1b;
                op1.setPt( new LongPoint( Pt ) );
                op1b = op1.duplicate( DiscardLeft );
            }
        }

        if (Dir2 == Direction.LEFT_TO_RIGHT) {
            while (op2.next.getPt().getX() <= Pt.getX() && op2.next.getPt().getX() >= op2.getPt().getX() && op2.next.getPt().getY() == Pt.getY()) {
                op2 = op2.next;
            }
            if (DiscardLeft && op2.getPt().getX() != Pt.getX()) {
                op2 = op2.next;
            }
            op2b = op2.duplicate( !DiscardLeft );
            if (!op2b.getPt().equals( Pt )) {
                op2 = op2b;
                op2.setPt( new LongPoint( Pt ) );
                op2b = op2.duplicate( !DiscardLeft );
            }
            ;
        }
        else {
            while (op2.next.getPt().getX() >= Pt.getX() && op2.next.getPt().getX() <= op2.getPt().getX() && op2.next.getPt().getY() == Pt.getY()) {
                op2 = op2.next;
            }
            if (!DiscardLeft && op2.getPt().getX() != Pt.getX()) {
                op2 = op2.next;
            }
            op2b = op2.duplicate( DiscardLeft );
            if (!op2b.getPt().equals( Pt )) {
                op2 = op2b;
                op2.setPt( new LongPoint( Pt ) );
                op2b = op2.duplicate( DiscardLeft );
            }
            ;
        }
        ;

        if (Dir1 == Direction.LEFT_TO_RIGHT == DiscardLeft) {
            op1.prev = op2;
            op2.next = op1;
            op1b.next = op2b;
            op2b.prev = op1b;
        }
        else {
            op1.next = op2;
            op2.prev = op1;
            op1b.prev = op2b;
            op2b.next = op1b;
        }
        return true;
    }

    private static boolean joinPoints( Join j, OutRec outRec1, OutRec outRec2 ) {
        Path.OutPt op1 = j.outPt1, op1b;
        Path.OutPt op2 = j.outPt2, op2b;

        //There are 3 kinds of joins for output polygons ...
        //1. Horizontal joins where Join.OutPt1 & Join.OutPt2 are a vertices anywhere
        //along (horizontal) collinear edges (& Join.OffPt is on the same horizontal).
        //2. Non-horizontal joins where Join.OutPt1 & Join.OutPt2 are at the same
        //location at the Bottom of the overlapping segment (& Join.OffPt is above).
        //3. StrictlySimple joins where edges touch but are not collinear and where
        //Join.OutPt1, Join.OutPt2 & Join.OffPt all share the same point.
        final boolean isHorizontal = j.outPt1.getPt().getY() == j.getOffPt().getY();

        if (isHorizontal && j.getOffPt().equals( j.outPt1.getPt() ) && j.getOffPt().equals( j.outPt2.getPt() )) {
            //Strictly Simple join ...
            if (outRec1 != outRec2) {
                return false;
            }
            op1b = j.outPt1.next;
            while (op1b != op1 && op1b.getPt().equals( j.getOffPt() )) {
                op1b = op1b.next;
            }
            final boolean reverse1 = op1b.getPt().getY() > j.getOffPt().getY();
            op2b = j.outPt2.next;
            while (op2b != op2 && op2b.getPt().equals( j.getOffPt() )) {
                op2b = op2b.next;
            }
            final boolean reverse2 = op2b.getPt().getY() > j.getOffPt().getY();
            if (reverse1 == reverse2) {
                return false;
            }
            if (reverse1) {
                op1b = op1.duplicate( false );
                op2b = op2.duplicate( true );
                op1.prev = op2;
                op2.next = op1;
                op1b.next = op2b;
                op2b.prev = op1b;
                j.outPt1 = op1;
                j.outPt2 = op1b;
                return true;
            }
            else {
                op1b = op1.duplicate( true );
                op2b = op2.duplicate( false );
                op1.next = op2;
                op2.prev = op1;
                op1b.prev = op2b;
                op2b.next = op1b;
                j.outPt1 = op1;
                j.outPt2 = op1b;
                return true;
            }
        }
        else if (isHorizontal) {
            //treat horizontal joins differently to non-horizontal joins since with
            //them we're not yet sure where the overlapping is. OutPt1.Pt & OutPt2.Pt
            //may be anywhere along the horizontal edge.
            op1b = op1;
            while (op1.prev.getPt().getY() == op1.getPt().getY() && op1.prev != op1b && op1.prev != op2) {
                op1 = op1.prev;
            }
            while (op1b.next.getPt().getY() == op1b.getPt().getY() && op1b.next != op1 && op1b.next != op2) {
                op1b = op1b.next;
            }
            if (op1b.next == op1 || op1b.next == op2) {
                return false;
            } //a flat 'polygon'

            op2b = op2;
            while (op2.prev.getPt().getY() == op2.getPt().getY() && op2.prev != op2b && op2.prev != op1b) {
                op2 = op2.prev;
            }
            while (op2b.next.getPt().getY() == op2b.getPt().getY() && op2b.next != op2 && op2b.next != op1) {
                op2b = op2b.next;
            }
            if (op2b.next == op2 || op2b.next == op1) {
                return false;
            } //a flat 'polygon'

            final long[] LeftV = new long[1], RightV = new long[1];
            //Op1 -. Op1b & Op2 -. Op2b are the extremites of the horizontal edges
            if (!getOverlap( op1.getPt().getX(), op1b.getPt().getX(), op2.getPt().getX(), op2b.getPt().getX(), LeftV, RightV )) {
                return false;
            }
            final long Left = LeftV[0];
            final long Right = RightV[0];

            //DiscardLeftSide: when overlapping edges are joined, a spike will created
            //which needs to be cleaned up. However, we don't want Op1 or Op2 caught up
            //on the discard Side as either may still be needed for other joins ...
            LongPoint Pt;
            boolean DiscardLeftSide;
            if (op1.getPt().getX() >= Left && op1.getPt().getX() <= Right) {
                Pt = new LongPoint( op1.getPt() );
                DiscardLeftSide = op1.getPt().getX() > op1b.getPt().getX();
            }
            else if (op2.getPt().getX() >= Left && op2.getPt().getX() <= Right) {
                Pt = new LongPoint( op2.getPt() );
                DiscardLeftSide = op2.getPt().getX() > op2b.getPt().getX();
            }
            else if (op1b.getPt().getX() >= Left && op1b.getPt().getX() <= Right) {
                Pt = new LongPoint( op1b.getPt() );
                DiscardLeftSide = op1b.getPt().getX() > op1.getPt().getX();
            }
            else {
                Pt = new LongPoint( op2b.getPt() );
                DiscardLeftSide = op2b.getPt().getX() > op2.getPt().getX();
            }
            j.outPt1 = op1;
            j.outPt2 = op2;
            return joinHorz( op1, op1b, op2, op2b, Pt, DiscardLeftSide );
        }
        else {
            //nb: For non-horizontal joins ...
            //    1. Jr.OutPt1.getPt().getY() == Jr.OutPt2.getPt().getY()
            //    2. Jr.OutPt1.Pt > Jr.OffPt.getY()

            //make sure the polygons are correctly oriented ...
            op1b = op1.next;
            while (op1b.getPt().equals( op1.getPt() ) && op1b != op1) {
                op1b = op1b.next;
            }
            final boolean Reverse1 = op1b.getPt().getY() > op1.getPt().getY() || !Point.slopesEqual( op1.getPt(), op1b.getPt(), j.getOffPt() );
            if (Reverse1) {
                op1b = op1.prev;
                while (op1b.getPt().equals( op1.getPt() ) && op1b != op1) {
                    op1b = op1b.prev;
                }
                if (op1b.getPt().getY() > op1.getPt().getY() || !Point.slopesEqual( op1.getPt(), op1b.getPt(), j.getOffPt() )) {
                    return false;
                }
            }
            ;
            op2b = op2.next;
            while (op2b.getPt().equals( op2.getPt() ) && op2b != op2) {
                op2b = op2b.next;
            }
            final boolean Reverse2 = op2b.getPt().getY() > op2.getPt().getY() || !Point.slopesEqual( op2.getPt(), op2b.getPt(), j.getOffPt() );
            if (Reverse2) {
                op2b = op2.prev;
                while (op2b.getPt().equals( op2.getPt() ) && op2b != op2) {
                    op2b = op2b.prev;
                }
                if (op2b.getPt().getY() > op2.getPt().getY() || !Point.slopesEqual( op2.getPt(), op2b.getPt(), j.getOffPt() )) {
                    return false;
                }
            }

            if (op1b == op1 || op2b == op2 || op1b == op2b || outRec1 == outRec2 && Reverse1 == Reverse2) {
                return false;
            }

            if (Reverse1) {
                op1b = op1.duplicate( false );
                op2b = op2.duplicate( true );
                op1.prev = op2;
                op2.next = op1;
                op1b.next = op2b;
                op2b.prev = op1b;
                j.outPt1 = op1;
                j.outPt2 = op1b;
                return true;
            }
            else {
                op1b = op1.duplicate( true );
                op2b = op2.duplicate( false );
                op1.next = op2;
                op2.prev = op1;
                op1b.prev = op2b;
                op2b.next = op1b;
                j.outPt1 = op1;
                j.outPt2 = op1b;
                return true;
            }
        }
    }

    private static Paths minkowski( Path pattern, Path path, boolean IsSum, boolean IsClosed ) {
        final int delta = IsClosed ? 1 : 0;
        final int polyCnt = pattern.size();
        final int pathCnt = path.size();
        final Paths result = new Paths( pathCnt );
        if (IsSum) {
            for (int i = 0; i < pathCnt; i++) {
                final Path p = new Path( polyCnt );
                for (final LongPoint ip : pattern) {
                    p.add( new LongPoint( path.get( i ).getX() + ip.getX(), path.get( i ).getY() + ip.getY(), 0 ) );
                }
                result.add( p );
            }
        }
        else {
            for (int i = 0; i < pathCnt; i++) {
                final Path p = new Path( polyCnt );
                for (final LongPoint ip : pattern) {
                    p.add( new LongPoint( path.get( i ).getX() - ip.getX(), path.get( i ).getY() - ip.getY(), 0 ) );
                }
                result.add( p );
            }
        }

        final Paths quads = new Paths( (pathCnt + delta) * (polyCnt + 1) );
        for (int i = 0; i < pathCnt - 1 + delta; i++) {
            for (int j = 0; j < polyCnt; j++) {
                final Path quad = new Path( 4 );
                quad.add( result.get( i % pathCnt ).get( j % polyCnt ) );
                quad.add( result.get( (i + 1) % pathCnt ).get( j % polyCnt ) );
                quad.add( result.get( (i + 1) % pathCnt ).get( (j + 1) % polyCnt ) );
                quad.add( result.get( i % pathCnt ).get( (j + 1) % polyCnt ) );
                if (!quad.orientation()) {
                    Collections.reverse( quad );
                }
                quads.add( quad );
            }
        }
        return quads;
    }

    public static Paths minkowskiDiff( Path poly1, Path poly2 ) {
        final Paths paths = minkowski( poly1, poly2, false, true );
        final DefaultClipper c = new DefaultClipper();
        c.addPaths( paths, PolyType.SUBJECT, true );
        c.execute( ClipType.UNION, paths, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO );
        return paths;
    }

    public static Paths minkowskiSum( Path pattern, Path path, boolean pathIsClosed ) {
        final Paths paths = minkowski( pattern, path, true, pathIsClosed );
        final DefaultClipper c = new DefaultClipper();
        c.addPaths( paths, PolyType.SUBJECT, true );
        c.execute( ClipType.UNION, paths, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO );
        return paths;
    }

    public static Paths minkowskiSum( Path pattern, Paths paths, boolean pathIsClosed ) {
        final Paths solution = new Paths();
        final DefaultClipper c = new DefaultClipper();
        for (int i = 0; i < paths.size(); ++i) {
            final Paths tmp = minkowski( pattern, paths.get( i ), true, pathIsClosed );
            c.addPaths( tmp, PolyType.SUBJECT, true );
            if (pathIsClosed) {
                final Path path = paths.get( i ).TranslatePath( pattern.get( 0 ) );
                c.addPath( path, PolyType.CLIP, true );
            }
        }
        c.execute( ClipType.UNION, solution, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO );
        return solution;
    }

    private static boolean poly2ContainsPoly1( Path.OutPt outPt1, Path.OutPt outPt2 ) {
        Path.OutPt op = outPt1;
        do {
            //nb: PointInPolygon returns 0 if false, +1 if true, -1 if pt on polygon
            final int res = isPointInPolygon( op.getPt(), outPt2 );
            if (res >= 0) {
                return res > 0;
            }
            op = op.next;
        }
        while (op != outPt1);
        return true;
    }

    //------------------------------------------------------------------------------
    // SimplifyPolygon functions ...
    // Convert self-intersecting polygons into simple polygons
    //------------------------------------------------------------------------------
    public static Paths simplifyPolygon( Path poly ) {
        return simplifyPolygon( poly, PolyFillType.EVEN_ODD );
    }

    public static Paths simplifyPolygon( Path poly, PolyFillType fillType ) {
        final Paths result = new Paths();
        final DefaultClipper c = new DefaultClipper( STRICTLY_SIMPLE );

        c.addPath( poly, PolyType.SUBJECT, true );
        c.execute( ClipType.UNION, result, fillType, fillType );
        return result;
    }

    public static Paths simplifyPolygons( Paths polys ) {
        return simplifyPolygons( polys, PolyFillType.EVEN_ODD );
    }

    public static Paths simplifyPolygons( Paths polys, PolyFillType fillType ) {
        final Paths result = new Paths();
        final DefaultClipper c = new DefaultClipper( STRICTLY_SIMPLE );

        c.addPaths( polys, PolyType.SUBJECT, true );
        c.execute( ClipType.UNION, result, fillType, fillType );
        return result;
    }

    private final List<OutRec> polyOuts;

    private ClipType clipType;

    private Scanbeam scanbeam;

    private Edge activeEdges;

    private Edge sortedEdges;

    private final List<IntersectNode> intersectList;

    private final Comparator<IntersectNode> intersectNodeComparer;

    private PolyFillType clipFillType;

    //------------------------------------------------------------------------------

    private PolyFillType subjFillType;

    //------------------------------------------------------------------------------

    private final List<Join> joins;

    //------------------------------------------------------------------------------

    private final List<Join> ghostJoins;

    private boolean usingPolyTree;

    public ZFillCallback zFillFunction;

    //------------------------------------------------------------------------------

    private final boolean reverseSolution;

    //------------------------------------------------------------------------------

    private final boolean strictlySimple;

    private final static Logger LOGGER = Logger.getLogger( DefaultClipper.class.getName() );

    public DefaultClipper() {
        this( 0 );
    }

    public DefaultClipper( int InitOptions ) //constructor
    {
        super( (PRESERVE_COLINEAR & InitOptions) != 0 );
        scanbeam = null;
        activeEdges = null;
        sortedEdges = null;
        intersectList = new ArrayList<IntersectNode>();
        intersectNodeComparer = new Comparator<IntersectNode>() {
            @Override
            public int compare(IntersectNode node1, IntersectNode node2) {
                final long i = node2.getPt().getY() - node1.getPt().getY();
                if (i > 0) {
                    return 1;
                }
                else if (i < 0) {
                    return -1;
                }
                return 0;
            }
        };

        usingPolyTree = false;
        polyOuts = new ArrayList<OutRec>();
        joins = new ArrayList<Join>();
        ghostJoins = new ArrayList<Join>();
        reverseSolution = (REVERSE_SOLUTION & InitOptions) != 0;
        strictlySimple = (STRICTLY_SIMPLE & InitOptions) != 0;

        zFillFunction = null;

    }

    private void addEdgeToSEL( Edge edge ) {
        LOGGER.entering( DefaultClipper.class.getName(), "addEdgeToSEL" );

        //SEL pointers in PEdge are reused to build a list of horizontal edges.
        //However, we don't need to worry about order with horizontal edge processing.
        if (sortedEdges == null) {
            sortedEdges = edge;
            edge.prevInSEL = null;
            edge.nextInSEL = null;
        }
        else {
            edge.nextInSEL = sortedEdges;
            edge.prevInSEL = null;
            sortedEdges.prevInSEL = edge;
            sortedEdges = edge;
        }
    }

    private void addGhostJoin( Path.OutPt Op, LongPoint OffPt ) {
        final Join j = new Join();
        j.outPt1 = Op;
        j.setOffPt( new LongPoint( OffPt ) );
        ghostJoins.add( j );
    }

    //------------------------------------------------------------------------------

    private void addJoin( Path.OutPt Op1, Path.OutPt Op2, LongPoint OffPt ) {
        LOGGER.entering( DefaultClipper.class.getName(), "addJoin" );
        final Join j = new Join();
        j.outPt1 = Op1;
        j.outPt2 = Op2;
        j.setOffPt( new LongPoint( OffPt ) );
        joins.add( j );
    }

    //------------------------------------------------------------------------------

    private void addLocalMaxPoly( Edge e1, Edge e2, LongPoint pt ) {
        addOutPt( e1, pt );
        if (e2.windDelta == 0) {
            addOutPt( e2, pt );
        }
        if (e1.outIdx == e2.outIdx) {
            e1.outIdx = Edge.UNASSIGNED;
            e2.outIdx = Edge.UNASSIGNED;
        }
        else if (e1.outIdx < e2.outIdx) {
            appendPolygon( e1, e2 );
        }
        else {
            appendPolygon( e2, e1 );
        }
    }

    //------------------------------------------------------------------------------

    private Path.OutPt addLocalMinPoly( Edge e1, Edge e2, LongPoint pt ) {
        LOGGER.entering( DefaultClipper.class.getName(), "addLocalMinPoly" );
        Path.OutPt result;
        Edge e, prevE;
        if (e2.isHorizontal() || e1.deltaX > e2.deltaX) {
            result = addOutPt( e1, pt );
            e2.outIdx = e1.outIdx;
            e1.side = Edge.Side.LEFT;
            e2.side = Edge.Side.RIGHT;
            e = e1;
            if (e.prevInAEL == e2) {
                prevE = e2.prevInAEL;
            }
            else {
                prevE = e.prevInAEL;
            }
        }
        else {
            result = addOutPt( e2, pt );
            e1.outIdx = e2.outIdx;
            e1.side = Edge.Side.RIGHT;
            e2.side = Edge.Side.LEFT;
            e = e2;
            if (e.prevInAEL == e1) {
                prevE = e1.prevInAEL;
            }
            else {
                prevE = e.prevInAEL;
            }
        }

        if (prevE != null && prevE.outIdx >= 0 && Edge.topX( prevE, pt.getY() ) == Edge.topX( e, pt.getY() ) && Edge.slopesEqual( e, prevE )
                        && e.windDelta != 0 && prevE.windDelta != 0) {
            final Path.OutPt outPt = addOutPt( prevE, pt );
            addJoin( result, outPt, e.getTop() );
        }
        return result;
    }

    private Path.OutPt addOutPt( Edge e, LongPoint pt ) {
        LOGGER.entering( DefaultClipper.class.getName(), "addOutPt" );
        final boolean ToFront = e.side == Edge.Side.LEFT;
        if (e.outIdx < 0) {
            final OutRec outRec = createOutRec();
            outRec.isOpen = e.windDelta == 0;
            final Path.OutPt newOp = new Path.OutPt();
            outRec.setPoints( newOp );
            newOp.idx = outRec.Idx;
            newOp.setPt( new LongPoint( pt ) );
            newOp.next = newOp;
            newOp.prev = newOp;
            if (!outRec.isOpen) {
                setHoleState( e, outRec );
            }
            e.outIdx = outRec.Idx; //nb: do this after SetZ !
            return newOp;
        }
        else {

            final OutRec outRec = polyOuts.get( e.outIdx );
            //OutRec.Pts is the 'Left-most' point & OutRec.Pts.Prev is the 'Right-most'
            final Path.OutPt op = outRec.getPoints();
            LOGGER.finest( "op=" + op.getPointCount() );
            LOGGER.finest( ToFront + " " + pt + " " + op.getPt() );
            if (ToFront && pt.equals( op.getPt() )) {
                return op;
            }
            else if (!ToFront && pt.equals( op.prev.getPt() )) {
                return op.prev;
            }

            final Path.OutPt newOp = new Path.OutPt();
            newOp.idx = outRec.Idx;
            newOp.setPt( new LongPoint( pt ) );
            newOp.next = op;
            newOp.prev = op.prev;
            newOp.prev.next = newOp;
            op.prev = newOp;
            if (ToFront) {
                outRec.setPoints( newOp );
            }
            return newOp;
        }
    }

    private void appendPolygon( Edge e1, Edge e2 ) {
        LOGGER.entering( DefaultClipper.class.getName(), "appendPolygon" );

        //get the start and ends of both output polygons ...
        final OutRec outRec1 = polyOuts.get( e1.outIdx );
        final OutRec outRec2 = polyOuts.get( e2.outIdx );
        LOGGER.finest( "" + e1.outIdx );
        LOGGER.finest( "" + e2.outIdx );

        OutRec holeStateRec;
        if (isParam1RightOfParam2( outRec1, outRec2 )) {
            holeStateRec = outRec2;
        }
        else if (isParam1RightOfParam2( outRec2, outRec1 )) {
            holeStateRec = outRec1;
        }
        else {
            holeStateRec = Path.OutPt.getLowerMostRec( outRec1, outRec2 );
        }

        final Path.OutPt p1_lft = outRec1.getPoints();
        final Path.OutPt p1_rt = p1_lft.prev;
        final Path.OutPt p2_lft = outRec2.getPoints();
        final Path.OutPt p2_rt = p2_lft.prev;

        LOGGER.finest( "p1_lft.getPointCount() = " + p1_lft.getPointCount() );
        LOGGER.finest( "p1_rt.getPointCount() = " + p1_rt.getPointCount() );
        LOGGER.finest( "p2_lft.getPointCount() = " + p2_lft.getPointCount() );
        LOGGER.finest( "p2_rt.getPointCount() = " + p2_rt.getPointCount() );

        Edge.Side side;
        //join e2 poly onto e1 poly and delete pointers to e2 ...
        if (e1.side == Edge.Side.LEFT) {
            if (e2.side == Edge.Side.LEFT) {
                //z y x a b c
                p2_lft.reversePolyPtLinks();
                p2_lft.next = p1_lft;
                p1_lft.prev = p2_lft;
                p1_rt.next = p2_rt;
                p2_rt.prev = p1_rt;
                outRec1.setPoints( p2_rt );
            }
            else {
                //x y z a b c
                p2_rt.next = p1_lft;
                p1_lft.prev = p2_rt;
                p2_lft.prev = p1_rt;
                p1_rt.next = p2_lft;
                outRec1.setPoints( p2_lft );
            }
            side = Edge.Side.LEFT;
        }
        else {
            if (e2.side == Edge.Side.RIGHT) {
                //a b c z y x
                p2_lft.reversePolyPtLinks();
                p1_rt.next = p2_rt;
                p2_rt.prev = p1_rt;
                p2_lft.next = p1_lft;
                p1_lft.prev = p2_lft;
            }
            else {
                //a b c x y z
                p1_rt.next = p2_lft;
                p2_lft.prev = p1_rt;
                p1_lft.prev = p2_rt;
                p2_rt.next = p1_lft;
            }
            side = Edge.Side.RIGHT;
        }
        outRec1.bottomPt = null;
        if (holeStateRec.equals( outRec2 )) {
            if (outRec2.firstLeft != outRec1) {
                outRec1.firstLeft = outRec2.firstLeft;
            }
            outRec1.isHole = outRec2.isHole;
        }
        outRec2.setPoints( null );
        outRec2.bottomPt = null;

        outRec2.firstLeft = outRec1;

        final int OKIdx = e1.outIdx;
        final int ObsoleteIdx = e2.outIdx;

        e1.outIdx = Edge.UNASSIGNED; //nb: safe because we only get here via AddLocalMaxPoly
        e2.outIdx = Edge.UNASSIGNED;

        Edge e = activeEdges;
        while (e != null) {
            if (e.outIdx == ObsoleteIdx) {
                e.outIdx = OKIdx;
                e.side = side;
                break;
            }
            e = e.nextInAEL;
        }
        outRec2.Idx = outRec1.Idx;
    }

    //------------------------------------------------------------------------------

    private void buildIntersectList( long topY ) {
        if (activeEdges == null) {
            return;
        }

        //prepare for sorting ...
        Edge e = activeEdges;
        sortedEdges = e;
        while (e != null) {
            e.prevInSEL = e.prevInAEL;
            e.nextInSEL = e.nextInAEL;
            e.getCurrent().setX( Edge.topX( e, topY ) );
            e = e.nextInAEL;
        }

        //bubblesort ...
        boolean isModified = true;
        while (isModified && sortedEdges != null) {
            isModified = false;
            e = sortedEdges;
            while (e.nextInSEL != null) {
                final Edge eNext = e.nextInSEL;
                final LongPoint[] pt = new LongPoint[1];
                if (e.getCurrent().getX() > eNext.getCurrent().getX()) {
                    intersectPoint( e, eNext, pt );
                    final IntersectNode newNode = new IntersectNode();
                    newNode.edge1 = e;
                    newNode.Edge2 = eNext;
                    newNode.setPt( new LongPoint( pt[0] ) );
                    intersectList.add( newNode );

                    swapPositionsInSEL( e, eNext );
                    isModified = true;
                }
                else {
                    e = eNext;
                }
            }
            if (e.prevInSEL != null) {
                e.prevInSEL.nextInSEL = null;
            }
            else {
                break;
            }
        }
        sortedEdges = null;
    }

    //------------------------------------------------------------------------------

    private void buildResult( Paths polyg ) {
        polyg.clear();
        for (int i = 0; i < polyOuts.size(); i++) {
            final OutRec outRec = polyOuts.get( i );
            if (outRec.getPoints() == null) {
                continue;
            }
            Path.OutPt p = outRec.getPoints().prev;
            final int cnt = p.getPointCount();
            LOGGER.finest( "cnt = " + cnt );
            if (cnt < 2) {
                continue;
            }
            final Path pg = new Path( cnt );
            for (int j = 0; j < cnt; j++) {
                pg.add( new LongPoint( p.getPt() ) );
                p = p.prev;
            }
            polyg.add( pg );
        }
    }

    private void buildResult2( PolyTree polytree ) {
        polytree.Clear();

        //add each output polygon/contour to polytree ...
        for (int i = 0; i < polyOuts.size(); i++) {
            final OutRec outRec = polyOuts.get( i );
            final int cnt = outRec.getPoints().getPointCount();
            if (outRec.isOpen && cnt < 2 || !outRec.isOpen && cnt < 3) {
                continue;
            }
            outRec.fixHoleLinkage();
            final PolyNode pn = new PolyNode();
            polytree.getAllPolys().add( pn );
            outRec.polyNode = pn;
            Path.OutPt op = outRec.getPoints().prev;
            for (int j = 0; j < cnt; j++) {
                pn.getPolygon().add( op.getPt() );
                op = op.prev;
            }
        }

        //fixup PolyNode links etc ...
        for (int i = 0; i < polyOuts.size(); i++) {
            final OutRec outRec = polyOuts.get( i );
            if (outRec.polyNode == null) {
                continue;
            }
            else if (outRec.isOpen) {
                outRec.polyNode.setOpen( true );
                polytree.addChild( outRec.polyNode );
            }
            else if (outRec.firstLeft != null && outRec.firstLeft.polyNode != null) {
                outRec.firstLeft.polyNode.addChild( outRec.polyNode );
            }
            else {
                polytree.addChild( outRec.polyNode );
            }
        }
    }

    private void copyAELToSEL() {
        Edge e = activeEdges;
        sortedEdges = e;
        while (e != null) {
            e.prevInSEL = e.prevInAEL;
            e.nextInSEL = e.nextInAEL;
            e = e.nextInAEL;
        }
    }

    private OutRec createOutRec() {
        final OutRec result = new OutRec();
        result.Idx = Edge.UNASSIGNED;
        result.isHole = false;
        result.isOpen = false;
        result.firstLeft = null;
        result.setPoints( null );
        result.bottomPt = null;
        result.polyNode = null;
        polyOuts.add( result );
        result.Idx = polyOuts.size() - 1;
        return result;
    }

    private void deleteFromAEL( Edge e ) {
        LOGGER.entering( DefaultClipper.class.getName(), "deleteFromAEL" );

        final Edge AelPrev = e.prevInAEL;
        final Edge AelNext = e.nextInAEL;
        if (AelPrev == null && AelNext == null && e != activeEdges) {
            return; //already deleted
        }
        if (AelPrev != null) {
            AelPrev.nextInAEL = AelNext;
        }
        else {
            activeEdges = AelNext;
        }
        if (AelNext != null) {
            AelNext.prevInAEL = AelPrev;
        }
        e.nextInAEL = null;
        e.prevInAEL = null;
        LOGGER.exiting( DefaultClipper.class.getName(), "deleteFromAEL" );
    }

    private void deleteFromSEL( Edge e ) {
        LOGGER.entering( DefaultClipper.class.getName(), "deleteFromSEL" );

        final Edge SelPrev = e.prevInSEL;
        final Edge SelNext = e.nextInSEL;
        if (SelPrev == null && SelNext == null && !e.equals( sortedEdges )) {
            return; //already deleted
        }
        if (SelPrev != null) {
            SelPrev.nextInSEL = SelNext;
        }
        else {
            sortedEdges = SelNext;
        }
        if (SelNext != null) {
            SelNext.prevInSEL = SelPrev;
        }
        e.nextInSEL = null;
        e.prevInSEL = null;
    }

    private boolean doHorzSegmentsOverlap( long seg1a, long seg1b, long seg2a, long seg2b ) {
        if (seg1a > seg1b) {
            final long tmp = seg1a;
            seg1a = seg1b;
            seg1b = tmp;
        }
        if (seg2a > seg2b) {
            final long tmp = seg2a;
            seg2a = seg2b;
            seg2b = tmp;
        }
        return seg1a < seg2b && seg2a < seg1b;
    }

    private void doMaxima( Edge e ) {
        final Edge eMaxPair = e.getMaximaPair();
        if (eMaxPair == null) {
            if (e.outIdx >= 0) {
                addOutPt( e, e.getTop() );
            }
            deleteFromAEL( e );
            return;
        }

        Edge eNext = e.nextInAEL;
        while (eNext != null && eNext != eMaxPair) {
            final LongPoint tmp = new LongPoint( e.getTop() );
            intersectEdges( e, eNext, tmp );
            e.setTop( new LongPoint( tmp ) );
            swapPositionsInAEL( e, eNext );
            eNext = e.nextInAEL;
        }

        if (e.outIdx == Edge.UNASSIGNED && eMaxPair.outIdx == Edge.UNASSIGNED) {
            deleteFromAEL( e );
            deleteFromAEL( eMaxPair );
        }
        else if (e.outIdx >= 0 && eMaxPair.outIdx >= 0) {
            if (e.outIdx >= 0) {
                addLocalMaxPoly( e, eMaxPair, e.getTop() );
            }
            deleteFromAEL( e );
            deleteFromAEL( eMaxPair );
        }

        else if (e.windDelta == 0) {
            if (e.outIdx >= 0) {
                addOutPt( e, e.getTop() );
                e.outIdx = Edge.UNASSIGNED;
            }
            deleteFromAEL( e );

            if (eMaxPair.outIdx >= 0) {
                addOutPt( eMaxPair, e.getTop() );
                eMaxPair.outIdx = Edge.UNASSIGNED;
            }
            deleteFromAEL( eMaxPair );
        }
        else {
            throw new IllegalStateException( "DoMaxima error" );
        }
    }

    //------------------------------------------------------------------------------

    private void doSimplePolygons() {
        int i = 0;
        while (i < polyOuts.size()) {
            final OutRec outrec = polyOuts.get( i++ );
            Path.OutPt op = outrec.getPoints();
            if (op == null || outrec.isOpen) {
                continue;
            }
            do //for each Pt in Polygon until duplicate found do ...
            {
                Path.OutPt op2 = op.next;
                while (op2 != outrec.getPoints()) {
                    if (op.getPt().equals( op2.getPt() ) && !op2.next.equals( op ) && !op2.prev.equals( op )) {
                        //split the polygon into two ...
                        final Path.OutPt op3 = op.prev;
                        final Path.OutPt op4 = op2.prev;
                        op.prev = op4;
                        op4.next = op;
                        op2.prev = op3;
                        op3.next = op2;

                        outrec.setPoints( op );
                        final OutRec outrec2 = createOutRec();
                        outrec2.setPoints( op2 );
                        updateOutPtIdxs( outrec2 );
                        if (poly2ContainsPoly1( outrec2.getPoints(), outrec.getPoints() )) {
                            //OutRec2 is contained by OutRec1 ...
                            outrec2.isHole = !outrec.isHole;
                            outrec2.firstLeft = outrec;
                            if (usingPolyTree) {
                                fixupFirstLefts2( outrec2, outrec );
                            }
                        }
                        else if (poly2ContainsPoly1( outrec.getPoints(), outrec2.getPoints() )) {
                            //OutRec1 is contained by OutRec2 ...
                            outrec2.isHole = outrec.isHole;
                            outrec.isHole = !outrec2.isHole;
                            outrec2.firstLeft = outrec.firstLeft;
                            outrec.firstLeft = outrec2;
                            if (usingPolyTree) {
                                fixupFirstLefts2( outrec, outrec2 );
                            }
                        }
                        else {
                            //the 2 polygons are separate ...
                            outrec2.isHole = outrec.isHole;
                            outrec2.firstLeft = outrec.firstLeft;
                            if (usingPolyTree) {
                                fixupFirstLefts1( outrec, outrec2 );
                            }
                        }
                        op2 = op; //ie get ready for the next iteration
                    }
                    op2 = op2.next;
                }
                op = op.next;
            }
            while (op != outrec.getPoints());
        }
    }

    //------------------------------------------------------------------------------

    private boolean EdgesAdjacent( IntersectNode inode ) {
        return inode.edge1.nextInSEL == inode.Edge2 || inode.edge1.prevInSEL == inode.Edge2;
    }

    //------------------------------------------------------------------------------

    @Override
    public boolean execute( ClipType clipType, Paths solution ) {
        return execute( clipType, solution, PolyFillType.EVEN_ODD, PolyFillType.EVEN_ODD );
    }

    @Override
    public boolean execute( ClipType clipType, Paths solution, PolyFillType subjFillType, PolyFillType clipFillType ) {

        synchronized (this) {

            if (hasOpenPaths) {
                throw new IllegalStateException( "Error: PolyTree struct is need for open path clipping." );
            }

            solution.clear();
            this.subjFillType = subjFillType;
            this.clipFillType = clipFillType;
            this.clipType = clipType;
            usingPolyTree = false;
            boolean succeeded;
            try {
                succeeded = executeInternal();
                //build the return polygons ...
                if (succeeded) {
                    buildResult( solution );
                }
                return succeeded;
            }
            finally {
                polyOuts.clear();

            }
        }

    }

    @Override
    public boolean execute( ClipType clipType, PolyTree polytree ) {
        return execute( clipType, polytree, PolyFillType.EVEN_ODD, PolyFillType.EVEN_ODD );
    }

    @Override
    public boolean execute( ClipType clipType, PolyTree polytree, PolyFillType subjFillType, PolyFillType clipFillType ) {
        synchronized (this) {
            this.subjFillType = subjFillType;
            this.clipFillType = clipFillType;
            this.clipType = clipType;
            usingPolyTree = true;
            boolean succeeded;
            try {
                succeeded = executeInternal();
                //build the return polygons ...
                if (succeeded) {
                    buildResult2( polytree );
                }
            }
            finally {
                polyOuts.clear();
            }
            return succeeded;
        }
    }

    //------------------------------------------------------------------------------

    private boolean executeInternal() {
        try {
            reset();
            if (currentLM == null) {
                return false;
            }

            long botY = popScanbeam();

            do {
                insertLocalMinimaIntoAEL( botY );
                ghostJoins.clear();
                processHorizontals( false );
                if (scanbeam == null) {
                    break;
                }
                final long topY = popScanbeam();
                if (!processIntersections( topY )) {
                    return false;
                }
                processEdgesAtTopOfScanbeam( topY );
                botY = topY;
            }
            while (scanbeam != null || currentLM != null);

            for (int i = 0; i < polyOuts.size(); i++) {
                final OutRec outRec = polyOuts.get( i );
                if (outRec.getPoints() == null || outRec.isOpen) {
                    continue;
                }
                LOGGER.finest( "I=" + outRec.getPoints().getPointCount() );
            }
            //fix orientations ...
            for (int i = 0; i < polyOuts.size(); i++) {
                final OutRec outRec = polyOuts.get( i );
                if (outRec.getPoints() == null || outRec.isOpen) {
                    continue;
                }
                if ((outRec.isHole ^ reverseSolution) == outRec.area() > 0) {
                    outRec.getPoints().reversePolyPtLinks();
                }
            }

            joinCommonEdges();

            for (int i = 0; i < polyOuts.size(); i++) {
                final OutRec outRec = polyOuts.get( i );
                if (outRec.getPoints() != null && !outRec.isOpen) {
                    fixupOutPolygon( outRec );
                }
            }

            if (strictlySimple) {
                doSimplePolygons();
            }
            return true;
        }
        //catch { return false; }
        finally {
            joins.clear();
            ghostJoins.clear();
        }
    }

    //------------------------------------------------------------------------------

    private void fixupFirstLefts1( OutRec OldOutRec, OutRec NewOutRec ) {
        for (int i = 0; i < polyOuts.size(); i++) {
            final OutRec outRec = polyOuts.get( i );
            if (outRec.getPoints() == null || outRec.firstLeft == null) {
                continue;
            }
            final OutRec firstLeft = outRec.firstLeft.parseFirstLeft();
            if (firstLeft.equals( OldOutRec )) {
                if (poly2ContainsPoly1( outRec.getPoints(), NewOutRec.getPoints() )) {
                    outRec.firstLeft = NewOutRec;
                }
            }
        }
    }

    private void fixupFirstLefts2( OutRec OldOutRec, OutRec NewOutRec ) {
        for (final OutRec outRec : polyOuts) {
            if (outRec.firstLeft.equals( OldOutRec )) {
                outRec.firstLeft = NewOutRec;
            }
        }
    }

    private boolean fixupIntersectionOrder() {
        //pre-condition: intersections are sorted bottom-most first.
        //Now it's crucial that intersections are made only between adjacent edges,
        //so to ensure this the order of intersections may need adjusting ...
        Collections.sort( intersectList, intersectNodeComparer );

        copyAELToSEL();
        final int cnt = intersectList.size();
        for (int i = 0; i < cnt; i++) {
            if (!EdgesAdjacent( intersectList.get( i ) )) {
                int j = i + 1;
                while (j < cnt && !EdgesAdjacent( intersectList.get( j ) )) {
                    j++;
                }
                if (j == cnt) {
                    return false;
                }

                final IntersectNode tmp = intersectList.get( i );
                intersectList.set( i, intersectList.get( j ) );
                intersectList.set( j, tmp );

            }
            swapPositionsInSEL( intersectList.get( i ).edge1, intersectList.get( i ).Edge2 );
        }
        return true;
    }

    //----------------------------------------------------------------------

    private void fixupOutPolygon( OutRec outRec ) {
        //FixupOutPolygon() - removes duplicate points and simplifies consecutive
        //parallel edges by removing the middle vertex.
        Path.OutPt lastOK = null;
        outRec.bottomPt = null;
        Path.OutPt pp = outRec.getPoints();
        for (;;) {
            if (pp.prev == pp || pp.prev == pp.next) {
                outRec.setPoints( null );
                return;
            }
            //test for duplicate points and collinear edges ...
            if (pp.getPt().equals( pp.next.getPt() ) || pp.getPt().equals( pp.prev.getPt() )
                            || Point.slopesEqual( pp.prev.getPt(), pp.getPt(), pp.next.getPt() )
                            && (!preserveCollinear || !Point.isPt2BetweenPt1AndPt3( pp.prev.getPt(), pp.getPt(), pp.next.getPt() ))) {
                lastOK = null;
                pp.prev.next = pp.next;
                pp.next.prev = pp.prev;
                pp = pp.prev;
            }
            else if (pp == lastOK) {
                break;
            }
            else {
                if (lastOK == null) {
                    lastOK = pp;
                }
                pp = pp.next;
            }
        }
        outRec.setPoints( pp );
    }

    private OutRec getOutRec( int idx ) {
        OutRec outrec = polyOuts.get( idx );
        while (outrec != polyOuts.get( outrec.Idx )) {
            outrec = polyOuts.get( outrec.Idx );
        }
        return outrec;
    }

    private void insertEdgeIntoAEL( Edge edge, Edge startEdge ) {
        LOGGER.entering( DefaultClipper.class.getName(), "insertEdgeIntoAEL" );

        if (activeEdges == null) {
            edge.prevInAEL = null;
            edge.nextInAEL = null;
            LOGGER.finest( "Edge " + edge.outIdx + " -> " + null );
            activeEdges = edge;
        }
        else if (startEdge == null && Edge.doesE2InsertBeforeE1( activeEdges, edge )) {
            edge.prevInAEL = null;
            edge.nextInAEL = activeEdges;
            LOGGER.finest( "Edge " + edge.outIdx + " -> " + edge.nextInAEL.outIdx );
            activeEdges.prevInAEL = edge;
            activeEdges = edge;
        }
        else {
            LOGGER.finest( "activeEdges unchanged" );
            if (startEdge == null) {
                startEdge = activeEdges;
            }
            while (startEdge.nextInAEL != null && !Edge.doesE2InsertBeforeE1( startEdge.nextInAEL, edge )) {
                startEdge = startEdge.nextInAEL;
            }
            edge.nextInAEL = startEdge.nextInAEL;
            if (startEdge.nextInAEL != null) {
                startEdge.nextInAEL.prevInAEL = edge;
            }
            edge.prevInAEL = startEdge;
            startEdge.nextInAEL = edge;
        }
    }

    //------------------------------------------------------------------------------

    private void insertLocalMinimaIntoAEL( long botY ) {
        LOGGER.entering( DefaultClipper.class.getName(), "insertLocalMinimaIntoAEL" );

        while (currentLM != null && currentLM.y == botY) {
            final Edge lb = currentLM.leftBound;
            final Edge rb = currentLM.rightBound;
            popLocalMinima();

            Path.OutPt Op1 = null;
            if (lb == null) {
                insertEdgeIntoAEL( rb, null );
                updateWindingCount( rb );
                if (rb.isContributing( clipFillType, subjFillType, clipType )) {
                    Op1 = addOutPt( rb, rb.getBot() );
                }
            }
            else if (rb == null) {
                insertEdgeIntoAEL( lb, null );
                updateWindingCount( lb );
                if (lb.isContributing( clipFillType, subjFillType, clipType )) {
                    Op1 = addOutPt( lb, lb.getBot() );
                }
                insertScanbeam( lb.getTop().getY() );
            }
            else {
                insertEdgeIntoAEL( lb, null );
                insertEdgeIntoAEL( rb, lb );
                updateWindingCount( lb );
                rb.windCnt = lb.windCnt;
                rb.windCnt2 = lb.windCnt2;
                if (lb.isContributing( clipFillType, subjFillType, clipType )) {
                    Op1 = addLocalMinPoly( lb, rb, lb.getBot() );
                }
                insertScanbeam( lb.getTop().getY() );
            }

            if (rb != null) {
                if (rb.isHorizontal()) {
                    addEdgeToSEL( rb );
                }
                else {
                    insertScanbeam( rb.getTop().getY() );
                }
            }

            if (lb == null || rb == null) {
                continue;
            }

            //if output polygons share an Edge with a horizontal rb, they'll need joining later ...
            if (Op1 != null && rb.isHorizontal() && ghostJoins.size() > 0 && rb.windDelta != 0) {
                for (int i = 0; i < ghostJoins.size(); i++) {
                    //if the horizontal Rb and a 'ghost' horizontal overlap, then convert
                    //the 'ghost' join to a real join ready for later ...
                    final Join j = ghostJoins.get( i );
                    if (doHorzSegmentsOverlap( j.outPt1.getPt().getX(), j.getOffPt().getX(), rb.getBot().getX(), rb.getTop().getX() )) {
                        addJoin( j.outPt1, Op1, j.getOffPt() );
                    }
                }
            }

            if (lb.outIdx >= 0 && lb.prevInAEL != null && lb.prevInAEL.getCurrent().getX() == lb.getBot().getX() && lb.prevInAEL.outIdx >= 0
                            && Edge.slopesEqual( lb.prevInAEL, lb ) && lb.windDelta != 0 && lb.prevInAEL.windDelta != 0) {
                final Path.OutPt Op2 = addOutPt( lb.prevInAEL, lb.getBot() );
                addJoin( Op1, Op2, lb.getTop() );
            }

            if (lb.nextInAEL != rb) {

                if (rb.outIdx >= 0 && rb.prevInAEL.outIdx >= 0 && Edge.slopesEqual( rb.prevInAEL, rb ) && rb.windDelta != 0 && rb.prevInAEL.windDelta != 0) {
                    final Path.OutPt Op2 = addOutPt( rb.prevInAEL, rb.getBot() );
                    addJoin( Op1, Op2, rb.getTop() );
                }

                Edge e = lb.nextInAEL;
                if (e != null) {
                    while (e != rb) {
                        //nb: For calculating winding counts etc, IntersectEdges() assumes
                        //that param1 will be to the right of param2 ABOVE the intersection ...
                        //nb: For calculating winding counts etc, IntersectEdges() assumes
                        //that param1 will be to the right of param2 ABOVE the intersection ...
                        intersectEdges( rb, e, lb.getCurrent() ); //order important here
                        e = e.nextInAEL;

                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------------

    private void insertScanbeam( long y ) {
        LOGGER.entering( DefaultClipper.class.getName(), "insertScanbeam" );

        if (scanbeam == null) {
            scanbeam = new Scanbeam();
            scanbeam.next = null;
            scanbeam.y = y;
        }
        else if (y > scanbeam.y) {
            final Scanbeam newSb = new Scanbeam();
            newSb.y = y;
            newSb.next = scanbeam;
            scanbeam = newSb;
        }
        else {
            Scanbeam sb2 = scanbeam;
            while (sb2.next != null && y <= sb2.next.y) {
                sb2 = sb2.next;
            }
            if (y == sb2.y) {
                return; //ie ignores duplicates
            }
            final Scanbeam newSb = new Scanbeam();
            newSb.y = y;
            newSb.next = sb2.next;
            sb2.next = newSb;
        }
    }

    //------------------------------------------------------------------------------

    private void intersectEdges( Edge e1, Edge e2, LongPoint pt ) {
        LOGGER.entering( DefaultClipper.class.getName(), "insersectEdges" );

        //e1 will be to the left of e2 BELOW the intersection. Therefore e1 is before
        //e2 in AEL except when e1 is being inserted at the intersection point ...

        final boolean e1Contributing = e1.outIdx >= 0;
        final boolean e2Contributing = e2.outIdx >= 0;

        setZ( pt, e1, e2 );

        //if either edge is on an OPEN path ...
        if (e1.windDelta == 0 || e2.windDelta == 0) {
            //ignore subject-subject open path intersections UNLESS they
            //are both open paths, AND they are both 'contributing maximas' ...
            if (e1.windDelta == 0 && e2.windDelta == 0) {
                return;
            }
            else if (e1.polyTyp == e2.polyTyp && e1.windDelta != e2.windDelta && clipType == ClipType.UNION) {
                if (e1.windDelta == 0) {
                    if (e2Contributing) {
                        addOutPt( e1, pt );
                        if (e1Contributing) {
                            e1.outIdx = Edge.UNASSIGNED;
                        }
                    }
                }
                else {
                    if (e1Contributing) {
                        addOutPt( e2, pt );
                        if (e2Contributing) {
                            e2.outIdx = Edge.UNASSIGNED;
                        }
                    }
                }
            }
            else if (e1.polyTyp != e2.polyTyp) {
                if (e1.windDelta == 0 && Math.abs( e2.windCnt ) == 1 && (clipType != ClipType.UNION || e2.windCnt2 == 0)) {
                    addOutPt( e1, pt );
                    if (e1Contributing) {
                        e1.outIdx = Edge.UNASSIGNED;
                    }
                }
                else if (e2.windDelta == 0 && Math.abs( e1.windCnt ) == 1 && (clipType != ClipType.UNION || e1.windCnt2 == 0)) {
                    addOutPt( e2, pt );
                    if (e2Contributing) {
                        e2.outIdx = Edge.UNASSIGNED;
                    }
                }
            }
            return;
        }

        //update winding counts...
        //assumes that e1 will be to the Right of e2 ABOVE the intersection
        if (e1.polyTyp == e2.polyTyp) {
            if (e1.isEvenOddFillType( clipFillType, subjFillType )) {
                final int oldE1WindCnt = e1.windCnt;
                e1.windCnt = e2.windCnt;
                e2.windCnt = oldE1WindCnt;
            }
            else {
                if (e1.windCnt + e2.windDelta == 0) {
                    e1.windCnt = -e1.windCnt;
                }
                else {
                    e1.windCnt += e2.windDelta;
                }
                if (e2.windCnt - e1.windDelta == 0) {
                    e2.windCnt = -e2.windCnt;
                }
                else {
                    e2.windCnt -= e1.windDelta;
                }
            }
        }
        else {
            if (!e2.isEvenOddFillType( clipFillType, subjFillType )) {
                e1.windCnt2 += e2.windDelta;
            }
            else {
                e1.windCnt2 = e1.windCnt2 == 0 ? 1 : 0;
            }
            if (!e1.isEvenOddFillType( clipFillType, subjFillType )) {
                e2.windCnt2 -= e1.windDelta;
            }
            else {
                e2.windCnt2 = e2.windCnt2 == 0 ? 1 : 0;
            }
        }

        PolyFillType e1FillType, e2FillType, e1FillType2, e2FillType2;
        if (e1.polyTyp == PolyType.SUBJECT) {
            e1FillType = subjFillType;
            e1FillType2 = clipFillType;
        }
        else {
            e1FillType = clipFillType;
            e1FillType2 = subjFillType;
        }
        if (e2.polyTyp == PolyType.SUBJECT) {
            e2FillType = subjFillType;
            e2FillType2 = clipFillType;
        }
        else {
            e2FillType = clipFillType;
            e2FillType2 = subjFillType;
        }

        int e1Wc, e2Wc;
        switch (e1FillType) {
            case POSITIVE:
                e1Wc = e1.windCnt;
                break;
            case NEGATIVE:
                e1Wc = -e1.windCnt;
                break;
            default:
                e1Wc = Math.abs( e1.windCnt );
                break;
        }
        switch (e2FillType) {
            case POSITIVE:
                e2Wc = e2.windCnt;
                break;
            case NEGATIVE:
                e2Wc = -e2.windCnt;
                break;
            default:
                e2Wc = Math.abs( e2.windCnt );
                break;
        }

        if (e1Contributing && e2Contributing) {
            if (e1Wc != 0 && e1Wc != 1 || e2Wc != 0 && e2Wc != 1 || e1.polyTyp != e2.polyTyp && clipType != ClipType.XOR) {
                addLocalMaxPoly( e1, e2, pt );
            }
            else {
                addOutPt( e1, pt );
                addOutPt( e2, pt );
                Edge.swapSides( e1, e2 );
                Edge.swapPolyIndexes( e1, e2 );
            }
        }
        else if (e1Contributing) {
            if (e2Wc == 0 || e2Wc == 1) {
                addOutPt( e1, pt );
                Edge.swapSides( e1, e2 );
                Edge.swapPolyIndexes( e1, e2 );
            }

        }
        else if (e2Contributing) {
            if (e1Wc == 0 || e1Wc == 1) {
                addOutPt( e2, pt );
                Edge.swapSides( e1, e2 );
                Edge.swapPolyIndexes( e1, e2 );
            }
        }
        else if ((e1Wc == 0 || e1Wc == 1) && (e2Wc == 0 || e2Wc == 1)) {
            //neither edge is currently contributing ...
            int e1Wc2, e2Wc2;
            switch (e1FillType2) {
                case POSITIVE:
                    e1Wc2 = e1.windCnt2;
                    break;
                case NEGATIVE:
                    e1Wc2 = -e1.windCnt2;
                    break;
                default:
                    e1Wc2 = Math.abs( e1.windCnt2 );
                    break;
            }
            switch (e2FillType2) {
                case POSITIVE:
                    e2Wc2 = e2.windCnt2;
                    break;
                case NEGATIVE:
                    e2Wc2 = -e2.windCnt2;
                    break;
                default:
                    e2Wc2 = Math.abs( e2.windCnt2 );
                    break;
            }

            if (e1.polyTyp != e2.polyTyp) {
                addLocalMinPoly( e1, e2, pt );
            }
            else if (e1Wc == 1 && e2Wc == 1) {
                switch (clipType) {
                    case INTERSECTION:
                        if (e1Wc2 > 0 && e2Wc2 > 0) {
                            addLocalMinPoly( e1, e2, pt );
                        }
                        break;
                    case UNION:
                        if (e1Wc2 <= 0 && e2Wc2 <= 0) {
                            addLocalMinPoly( e1, e2, pt );
                        }
                        break;
                    case DIFFERENCE:
                        if (e1.polyTyp == PolyType.CLIP && e1Wc2 > 0 && e2Wc2 > 0 || e1.polyTyp == PolyType.SUBJECT && e1Wc2 <= 0 && e2Wc2 <= 0) {
                            addLocalMinPoly( e1, e2, pt );
                        }
                        break;
                    case XOR:
                        addLocalMinPoly( e1, e2, pt );
                        break;
                }
            }
            else {
                Edge.swapSides( e1, e2 );
            }
        }
    }

    private void intersectPoint( Edge edge1, Edge edge2, LongPoint[] ipV ) {
        final LongPoint ip = ipV[0] = new LongPoint();

        double b1, b2;
        //nb: with very large coordinate values, it's possible for SlopesEqual() to
        //return false but for the edge.Dx value be equal due to double precision rounding.
        if (edge1.deltaX == edge2.deltaX) {
            ip.setY( edge1.getCurrent().getY() );
            ip.setX( Edge.topX( edge1, ip.getY() ) );
            return;
        }

        if (edge1.getDelta().getX() == 0) {
            ip.setX( edge1.getBot().getX() );
            if (edge2.isHorizontal()) {
                ip.setY( edge2.getBot().getY() );
            }
            else {
                b2 = edge2.getBot().getY() - edge2.getBot().getX() / edge2.deltaX;
                ip.setY( Math.round( ip.getX() / edge2.deltaX + b2 ) );
            }
        }
        else if (edge2.getDelta().getX() == 0) {
            ip.setX( edge2.getBot().getX() );
            if (edge1.isHorizontal()) {
                ip.setY( edge1.getBot().getY() );
            }
            else {
                b1 = edge1.getBot().getY() - edge1.getBot().getX() / edge1.deltaX;
                ip.setY( Math.round( ip.getX() / edge1.deltaX + b1 ) );
            }
        }
        else {
            b1 = edge1.getBot().getX() - edge1.getBot().getY() * edge1.deltaX;
            b2 = edge2.getBot().getX() - edge2.getBot().getY() * edge2.deltaX;
            final double q = (b2 - b1) / (edge1.deltaX - edge2.deltaX);
            ip.setY( Math.round( q ) );
            if (Math.abs( edge1.deltaX ) < Math.abs( edge2.deltaX )) {
                ip.setX( Math.round( edge1.deltaX * q + b1 ) );
            }
            else {
                ip.setX( Math.round( edge2.deltaX * q + b2 ) );
            }
        }

        if (ip.getY() < edge1.getTop().getY() || ip.getY() < edge2.getTop().getY()) {
            if (edge1.getTop().getY() > edge2.getTop().getY()) {
                ip.setY( edge1.getTop().getY() );
            }
            else {
                ip.setY( edge2.getTop().getY() );
            }
            if (Math.abs( edge1.deltaX ) < Math.abs( edge2.deltaX )) {
                ip.setX( Edge.topX( edge1, ip.getY() ) );
            }
            else {
                ip.setX( Edge.topX( edge2, ip.getY() ) );
            }
        }
        //finally, don't allow 'ip' to be BELOW curr.getY() (ie bottom of scanbeam) ...
        if (ip.getY() > edge1.getCurrent().getY()) {
            ip.setY( edge1.getCurrent().getY() );
            //better to use the more vertical edge to derive X ...
            if (Math.abs( edge1.deltaX ) > Math.abs( edge2.deltaX )) {
                ip.setX( Edge.topX( edge2, ip.getY() ) );
            }
            else {
                ip.setX( Edge.topX( edge1, ip.getY() ) );
            }
        }
    }

    private void joinCommonEdges() {
        for (int i = 0; i < joins.size(); i++) {
            final Join join = joins.get( i );

            final OutRec outRec1 = getOutRec( join.outPt1.idx );
            OutRec outRec2 = getOutRec( join.outPt2.idx );

            if (outRec1.getPoints() == null || outRec2.getPoints() == null) {
                continue;
            }

            //get the polygon fragment with the correct hole state (FirstLeft)
            //before calling JoinPoints() ...
            OutRec holeStateRec;
            if (outRec1 == outRec2) {
                holeStateRec = outRec1;
            }
            else if (isParam1RightOfParam2( outRec1, outRec2 )) {
                holeStateRec = outRec2;
            }
            else if (isParam1RightOfParam2( outRec2, outRec1 )) {
                holeStateRec = outRec1;
            }
            else {
                holeStateRec = Path.OutPt.getLowerMostRec( outRec1, outRec2 );
            }

            if (!joinPoints( join, outRec1, outRec2 )) {
                continue;
            }

            if (outRec1 == outRec2) {
                //instead of joining two polygons, we've just created a new one by
                //splitting one polygon into two.
                outRec1.setPoints( join.outPt1 );
                outRec1.bottomPt = null;
                outRec2 = createOutRec();
                outRec2.setPoints( join.outPt2 );

                //update all OutRec2.Pts Idx's ...
                updateOutPtIdxs( outRec2 );

                //We now need to check every OutRec.FirstLeft pointer. If it points
                //to OutRec1 it may need to point to OutRec2 instead ...
                if (usingPolyTree) {
                    for (int j = 0; j < polyOuts.size() - 1; j++) {
                        final OutRec oRec = polyOuts.get( j );
                        if (oRec.getPoints() == null || oRec.firstLeft.parseFirstLeft() != outRec1 || oRec.isHole == outRec1.isHole) {
                            continue;
                        }
                        if (poly2ContainsPoly1( oRec.getPoints(), join.outPt2 )) {
                            oRec.firstLeft = outRec2;
                        }
                    }
                }

                if (poly2ContainsPoly1( outRec2.getPoints(), outRec1.getPoints() )) {
                    //outRec2 is contained by outRec1 ...
                    outRec2.isHole = !outRec1.isHole;
                    outRec2.firstLeft = outRec1;

                    //fixup FirstLeft pointers that may need reassigning to OutRec1
                    if (usingPolyTree) {
                        fixupFirstLefts2( outRec2, outRec1 );
                    }

                    if ((outRec2.isHole ^ reverseSolution) == outRec2.area() > 0) {
                        outRec2.getPoints().reversePolyPtLinks();
                    }

                }
                else if (poly2ContainsPoly1( outRec1.getPoints(), outRec2.getPoints() )) {
                    //outRec1 is contained by outRec2 ...
                    outRec2.isHole = outRec1.isHole;
                    outRec1.isHole = !outRec2.isHole;
                    outRec2.firstLeft = outRec1.firstLeft;
                    outRec1.firstLeft = outRec2;

                    //fixup FirstLeft pointers that may need reassigning to OutRec1
                    if (usingPolyTree) {
                        fixupFirstLefts2( outRec1, outRec2 );
                    }

                    if ((outRec1.isHole ^ reverseSolution) == outRec1.area() > 0) {
                        outRec1.getPoints().reversePolyPtLinks();
                    }
                }
                else {
                    //the 2 polygons are completely separate ...
                    outRec2.isHole = outRec1.isHole;
                    outRec2.firstLeft = outRec1.firstLeft;

                    //fixup FirstLeft pointers that may need reassigning to OutRec2
                    if (usingPolyTree) {
                        fixupFirstLefts1( outRec1, outRec2 );
                    }
                }

            }
            else {
                //joined 2 polygons together ...

                outRec2.setPoints( null );
                outRec2.bottomPt = null;
                outRec2.Idx = outRec1.Idx;

                outRec1.isHole = holeStateRec.isHole;
                if (holeStateRec == outRec2) {
                    outRec1.firstLeft = outRec2.firstLeft;
                }
                outRec2.firstLeft = outRec1;

                //fixup FirstLeft pointers that may need reassigning to OutRec1
                if (usingPolyTree) {
                    fixupFirstLefts2( outRec2, outRec1 );
                }
            }
        }
    }

    private long popScanbeam() {
        LOGGER.entering( DefaultClipper.class.getName(), "popBeam" );

        final long y = scanbeam.y;
        scanbeam = scanbeam.next;
        return y;
    }

    private void processEdgesAtTopOfScanbeam( long topY ) {
        LOGGER.entering( DefaultClipper.class.getName(), "processEdgesAtTopOfScanbeam" );

        Edge e = activeEdges;
        while (e != null) {
            //1. process maxima, treating them as if they're 'bent' horizontal edges,
            //   but exclude maxima with horizontal edges. nb: e can't be a horizontal.
            boolean IsMaximaEdge = e.isMaxima( topY );

            if (IsMaximaEdge) {
                final Edge eMaxPair = e.getMaximaPair();
                IsMaximaEdge = eMaxPair == null || !eMaxPair.isHorizontal();
            }

            if (IsMaximaEdge) {
                final Edge ePrev = e.prevInAEL;
                doMaxima( e );
                if (ePrev == null) {
                    e = activeEdges;
                }
                else {
                    e = ePrev.nextInAEL;
                }
            }
            else {
                //2. promote horizontal edges, otherwise update Curr.getX() and Curr.getY() ...
                if (e.isIntermediate( topY ) && e.nextInLML.isHorizontal()) {
                    final Edge[] t = new Edge[] { e };
                    updateEdgeIntoAEL( t );
                    e = t[0];
                    if (e.outIdx >= 0) {
                        addOutPt( e, e.getBot() );
                    }
                    addEdgeToSEL( e );
                }
                else {
                    e.getCurrent().setX( Edge.topX( e, topY ) );
                    e.getCurrent().setY( topY );
                }

                if (strictlySimple) {
                    final Edge ePrev = e.prevInAEL;
                    if (e.outIdx >= 0 && e.windDelta != 0 && ePrev != null && ePrev.outIdx >= 0 && ePrev.getCurrent().getX() == e.getCurrent().getX()
                                    && ePrev.windDelta != 0) {
                        final LongPoint ip = new LongPoint( e.getCurrent() );

                        setZ( ip, ePrev, e );

                        final Path.OutPt op = addOutPt( ePrev, ip );
                        final Path.OutPt op2 = addOutPt( e, ip );
                        addJoin( op, op2, ip ); //StrictlySimple (type-3) join
                    }
                }

                e = e.nextInAEL;
            }
        }

        //3. Process horizontals at the Top of the scanbeam ...
        processHorizontals( true );

        //4. Promote intermediate vertices ...
        e = activeEdges;
        while (e != null) {
            if (e.isIntermediate( topY )) {
                Path.OutPt op = null;
                if (e.outIdx >= 0) {
                    op = addOutPt( e, e.getTop() );
                }
                final Edge[] t = new Edge[] { e };
                updateEdgeIntoAEL( t );
                e = t[0];

                //if output polygons share an edge, they'll need joining later ...
                final Edge ePrev = e.prevInAEL;
                final Edge eNext = e.nextInAEL;
                if (ePrev != null && ePrev.getCurrent().getX() == e.getBot().getX() && ePrev.getCurrent().getY() == e.getBot().getY() && op != null
                                && ePrev.outIdx >= 0 && ePrev.getCurrent().getY() > ePrev.getTop().getY() && Edge.slopesEqual( e, ePrev ) && e.windDelta != 0
                                && ePrev.windDelta != 0) {
                    final Path.OutPt op2 = addOutPt( ePrev, e.getBot() );
                    addJoin( op, op2, e.getTop() );
                }
                else if (eNext != null && eNext.getCurrent().getX() == e.getBot().getX() && eNext.getCurrent().getY() == e.getBot().getY() && op != null
                                && eNext.outIdx >= 0 && eNext.getCurrent().getY() > eNext.getTop().getY() && Edge.slopesEqual( e, eNext ) && e.windDelta != 0
                                && eNext.windDelta != 0) {
                    final Path.OutPt op2 = addOutPt( eNext, e.getBot() );
                    addJoin( op, op2, e.getTop() );
                }
            }
            e = e.nextInAEL;
        }
        LOGGER.exiting( DefaultClipper.class.getName(), "processEdgesAtTopOfScanbeam" );
    }

    private void processHorizontal( Edge horzEdge, boolean isTopOfScanbeam ) {
        LOGGER.entering( DefaultClipper.class.getName(), "isHorizontal" );
        final Direction[] dir = new Direction[1];
        final long[] horzLeft = new long[1], horzRight = new long[1];

        getHorzDirection( horzEdge, dir, horzLeft, horzRight );

        Edge eLastHorz = horzEdge, eMaxPair = null;
        while (eLastHorz.nextInLML != null && eLastHorz.nextInLML.isHorizontal()) {
            eLastHorz = eLastHorz.nextInLML;
        }
        if (eLastHorz.nextInLML == null) {
            eMaxPair = eLastHorz.getMaximaPair();
        }

        for (;;) {
            final boolean IsLastHorz = horzEdge == eLastHorz;
            Edge e = horzEdge.getNextInAEL( dir[0] );
            while (e != null) {
                //Break if we've got to the end of an intermediate horizontal edge ...
                //nb: Smaller Dx's are to the right of larger Dx's ABOVE the horizontal.
                if (e.getCurrent().getX() == horzEdge.getTop().getX() && horzEdge.nextInLML != null && e.deltaX < horzEdge.nextInLML.deltaX) {
                    break;
                }

                final Edge eNext = e.getNextInAEL( dir[0] ); //saves eNext for later

                if (dir[0] == Direction.LEFT_TO_RIGHT && e.getCurrent().getX() <= horzRight[0] || dir[0] == Direction.RIGHT_TO_LEFT
                                && e.getCurrent().getX() >= horzLeft[0]) {
                    //so far we're still in range of the horizontal Edge  but make sure
                    //we're at the last of consec. horizontals when matching with eMaxPair
                    if (e == eMaxPair && IsLastHorz) {
                        if (horzEdge.outIdx >= 0) {
                            final Path.OutPt op1 = addOutPt( horzEdge, horzEdge.getTop() );
                            Edge eNextHorz = sortedEdges;
                            while (eNextHorz != null) {
                                if (eNextHorz.outIdx >= 0
                                                && doHorzSegmentsOverlap( horzEdge.getBot().getX(), horzEdge.getTop().getX(), eNextHorz.getBot().getX(),
                                                                eNextHorz.getTop().getX() )) {
                                    final Path.OutPt op2 = addOutPt( eNextHorz, eNextHorz.getBot() );
                                    addJoin( op2, op1, eNextHorz.getTop() );
                                }
                                eNextHorz = eNextHorz.nextInSEL;
                            }
                            addGhostJoin( op1, horzEdge.getBot() );
                            addLocalMaxPoly( horzEdge, eMaxPair, horzEdge.getTop() );
                        }
                        deleteFromAEL( horzEdge );
                        deleteFromAEL( eMaxPair );
                        return;
                    }
                    else if (dir[0] == Direction.LEFT_TO_RIGHT) {
                        final LongPoint Pt = new LongPoint( e.getCurrent().getX(), horzEdge.getCurrent().getY() );
                        intersectEdges( horzEdge, e, Pt );
                    }
                    else {
                        final LongPoint Pt = new LongPoint( e.getCurrent().getX(), horzEdge.getCurrent().getY() );
                        intersectEdges( e, horzEdge, Pt );
                    }
                    swapPositionsInAEL( horzEdge, e );
                }
                else if (dir[0] == Direction.LEFT_TO_RIGHT && e.getCurrent().getX() >= horzRight[0] || dir[0] == Direction.RIGHT_TO_LEFT
                                && e.getCurrent().getX() <= horzLeft[0]) {
                    break;
                }
                e = eNext;
            } //end while

            if (horzEdge.nextInLML != null && horzEdge.nextInLML.isHorizontal()) {

                final Edge[] t = new Edge[] { horzEdge };
                updateEdgeIntoAEL( t );
                horzEdge = t[0];

                if (horzEdge.outIdx >= 0) {
                    addOutPt( horzEdge, horzEdge.getBot() );
                }
                getHorzDirection( horzEdge, dir, horzLeft, horzRight );
            }
            else {
                break;
            }
        } //end for (;;)

        if (horzEdge.nextInLML != null) {
            if (horzEdge.outIdx >= 0) {
                final Path.OutPt op1 = addOutPt( horzEdge, horzEdge.getTop() );
                if (isTopOfScanbeam) {
                    addGhostJoin( op1, horzEdge.getBot() );
                }
                final Edge[] t = new Edge[] { horzEdge };
                updateEdgeIntoAEL( t );
                horzEdge = t[0];

                if (horzEdge.windDelta == 0) {
                    return;
                }
                //nb: HorzEdge is no longer horizontal here
                final Edge ePrev = horzEdge.prevInAEL;
                final Edge eNext = horzEdge.nextInAEL;
                if (ePrev != null && ePrev.getCurrent().getX() == horzEdge.getBot().getX() && ePrev.getCurrent().getY() == horzEdge.getBot().getY()
                                && ePrev.windDelta != 0 && ePrev.outIdx >= 0 && ePrev.getCurrent().getY() > ePrev.getTop().getY()
                                && Edge.slopesEqual( horzEdge, ePrev )) {
                    final Path.OutPt op2 = addOutPt( ePrev, horzEdge.getBot() );
                    addJoin( op1, op2, horzEdge.getTop() );
                }
                else if (eNext != null && eNext.getCurrent().getX() == horzEdge.getBot().getX() && eNext.getCurrent().getY() == horzEdge.getBot().getY()
                                && eNext.windDelta != 0 && eNext.outIdx >= 0 && eNext.getCurrent().getY() > eNext.getTop().getY()
                                && Edge.slopesEqual( horzEdge, eNext )) {
                    final Path.OutPt op2 = addOutPt( eNext, horzEdge.getBot() );
                    addJoin( op1, op2, horzEdge.getTop() );
                }
            }
            else {
                final Edge[] t = new Edge[] { horzEdge };
                updateEdgeIntoAEL( t );
                horzEdge = t[0];
            }
        }
        else {
            if (horzEdge.outIdx >= 0) {
                addOutPt( horzEdge, horzEdge.getTop() );
            }
            deleteFromAEL( horzEdge );
        }
    }

    //------------------------------------------------------------------------------

    private void processHorizontals( boolean isTopOfScanbeam ) {
        LOGGER.entering( DefaultClipper.class.getName(), "processHorizontals" );

        Edge horzEdge = sortedEdges;
        while (horzEdge != null) {
            deleteFromSEL( horzEdge );
            processHorizontal( horzEdge, isTopOfScanbeam );
            horzEdge = sortedEdges;
        }
    }

    //------------------------------------------------------------------------------

    private boolean processIntersections( long topY ) {
        LOGGER.entering( DefaultClipper.class.getName(), "processIntersections" );

        if (activeEdges == null) {
            return true;
        }
        try {
            buildIntersectList( topY );
            if (intersectList.size() == 0) {
                return true;
            }
            if (intersectList.size() == 1 || fixupIntersectionOrder()) {
                processIntersectList();
            }
            else {
                return false;
            }
        }
        catch (final Exception e) {
            sortedEdges = null;
            intersectList.clear();
            throw new IllegalStateException( "ProcessIntersections error", e );
        }
        sortedEdges = null;
        return true;
    }

    private void processIntersectList() {
        for (int i = 0; i < intersectList.size(); i++) {
            final IntersectNode iNode = intersectList.get( i );
            {
                intersectEdges( iNode.edge1, iNode.Edge2, iNode.getPt() );
                swapPositionsInAEL( iNode.edge1, iNode.Edge2 );
            }
        }
        intersectList.clear();
    }

    //------------------------------------------------------------------------------

    @Override
    protected void reset() {
        super.reset();
        scanbeam = null;
        activeEdges = null;
        sortedEdges = null;
        LocalMinima lm = minimaList;
        while (lm != null) {
            insertScanbeam( lm.y );
            lm = lm.next;
        }
    }

    private void setHoleState( Edge e, OutRec outRec ) {
        boolean isHole = false;
        Edge e2 = e.prevInAEL;
        while (e2 != null) {
            if (e2.outIdx >= 0 && e2.windDelta != 0) {
                isHole = !isHole;
                if (outRec.firstLeft == null) {
                    outRec.firstLeft = polyOuts.get( e2.outIdx );
                }
            }
            e2 = e2.prevInAEL;
        }
        if (isHole) {
            outRec.isHole = true;
        }
    }

    private void setZ( LongPoint pt, Edge e1, Edge e2 ) {
        if (pt.getZ() != 0 || zFillFunction == null) {
            return;
        }
        else if (pt.equals( e1.getBot() )) {
            pt.setZ( e1.getBot().getZ() );
        }
        else if (pt.equals( e1.getTop() )) {
            pt.setZ( e1.getTop().getZ() );
        }
        else if (pt.equals( e2.getBot() )) {
            pt.setZ( e2.getBot().getZ() );
        }
        else if (pt.equals( e2.getTop() )) {
            pt.setZ( e2.getTop().getZ() );
        }
        else {
            zFillFunction.zFill( e1.getBot(), e1.getTop(), e2.getBot(), e2.getTop(), pt );
        }
    }

    private void swapPositionsInAEL( Edge edge1, Edge edge2 ) {
        LOGGER.entering( DefaultClipper.class.getName(), "swapPositionsInAEL" );

        //check that one or other edge hasn't already been removed from AEL ...
        if (edge1.nextInAEL == edge1.prevInAEL || edge2.nextInAEL == edge2.prevInAEL) {
            return;
        }

        if (edge1.nextInAEL == edge2) {
            final Edge next = edge2.nextInAEL;
            if (next != null) {
                next.prevInAEL = edge1;
            }
            final Edge prev = edge1.prevInAEL;
            if (prev != null) {
                prev.nextInAEL = edge2;
            }
            edge2.prevInAEL = prev;
            edge2.nextInAEL = edge1;
            edge1.prevInAEL = edge2;
            edge1.nextInAEL = next;
        }
        else if (edge2.nextInAEL == edge1) {
            final Edge next = edge1.nextInAEL;
            if (next != null) {
                next.prevInAEL = edge2;
            }
            final Edge prev = edge2.prevInAEL;
            if (prev != null) {
                prev.nextInAEL = edge1;
            }
            edge1.prevInAEL = prev;
            edge1.nextInAEL = edge2;
            edge2.prevInAEL = edge1;
            edge2.nextInAEL = next;
        }
        else {
            final Edge next = edge1.nextInAEL;
            final Edge prev = edge1.prevInAEL;
            edge1.nextInAEL = edge2.nextInAEL;
            if (edge1.nextInAEL != null) {
                edge1.nextInAEL.prevInAEL = edge1;
            }
            edge1.prevInAEL = edge2.prevInAEL;
            if (edge1.prevInAEL != null) {
                edge1.prevInAEL.nextInAEL = edge1;
            }
            edge2.nextInAEL = next;
            if (edge2.nextInAEL != null) {
                edge2.nextInAEL.prevInAEL = edge2;
            }
            edge2.prevInAEL = prev;
            if (edge2.prevInAEL != null) {
                edge2.prevInAEL.nextInAEL = edge2;
            }
        }

        if (edge1.prevInAEL == null) {
            activeEdges = edge1;
        }
        else if (edge2.prevInAEL == null) {
            activeEdges = edge2;
        }

        LOGGER.exiting( DefaultClipper.class.getName(), "swapPositionsInAEL" );
    }

    //------------------------------------------------------------------------------;

    private void swapPositionsInSEL( Edge edge1, Edge edge2 ) {
        if (edge1.nextInSEL == null && edge1.prevInSEL == null) {
            return;
        }
        if (edge2.nextInSEL == null && edge2.prevInSEL == null) {
            return;
        }

        if (edge1.nextInSEL == edge2) {
            final Edge next = edge2.nextInSEL;
            if (next != null) {
                next.prevInSEL = edge1;
            }
            final Edge prev = edge1.prevInSEL;
            if (prev != null) {
                prev.nextInSEL = edge2;
            }
            edge2.prevInSEL = prev;
            edge2.nextInSEL = edge1;
            edge1.prevInSEL = edge2;
            edge1.nextInSEL = next;
        }
        else if (edge2.nextInSEL == edge1) {
            final Edge next = edge1.nextInSEL;
            if (next != null) {
                next.prevInSEL = edge2;
            }
            final Edge prev = edge2.prevInSEL;
            if (prev != null) {
                prev.nextInSEL = edge1;
            }
            edge1.prevInSEL = prev;
            edge1.nextInSEL = edge2;
            edge2.prevInSEL = edge1;
            edge2.nextInSEL = next;
        }
        else {
            final Edge next = edge1.nextInSEL;
            final Edge prev = edge1.prevInSEL;
            edge1.nextInSEL = edge2.nextInSEL;
            if (edge1.nextInSEL != null) {
                edge1.nextInSEL.prevInSEL = edge1;
            }
            edge1.prevInSEL = edge2.prevInSEL;
            if (edge1.prevInSEL != null) {
                edge1.prevInSEL.nextInSEL = edge1;
            }
            edge2.nextInSEL = next;
            if (edge2.nextInSEL != null) {
                edge2.nextInSEL.prevInSEL = edge2;
            }
            edge2.prevInSEL = prev;
            if (edge2.prevInSEL != null) {
                edge2.prevInSEL.nextInSEL = edge2;
            }
        }

        if (edge1.prevInSEL == null) {
            sortedEdges = edge1;
        }
        else if (edge2.prevInSEL == null) {
            sortedEdges = edge2;
        }
    }

    private void updateEdgeIntoAEL( Edge[] eV ) {
        Edge e = eV[0];
        if (e.nextInLML == null) {
            throw new IllegalStateException( "UpdateEdgeIntoAEL: invalid call" );
        }
        final Edge AelPrev = e.prevInAEL;
        final Edge AelNext = e.nextInAEL;
        e.nextInLML.outIdx = e.outIdx;
        if (AelPrev != null) {
            AelPrev.nextInAEL = e.nextInLML;
        }
        else {
            activeEdges = e.nextInLML;
        }
        if (AelNext != null) {
            AelNext.prevInAEL = e.nextInLML;
        }
        e.nextInLML.side = e.side;
        e.nextInLML.windDelta = e.windDelta;
        e.nextInLML.windCnt = e.windCnt;
        e.nextInLML.windCnt2 = e.windCnt2;
        eV[0] = e = e.nextInLML;
        e.setCurrent( new LongPoint( e.getBot() ) );
        e.prevInAEL = AelPrev;
        e.nextInAEL = AelNext;
        if (!e.isHorizontal()) {
            insertScanbeam( e.getTop().getY() );
        }
    }

    private void updateOutPtIdxs( OutRec outrec ) {
        Path.OutPt op = outrec.getPoints();
        do {
            op.idx = outrec.Idx;
            op = op.prev;
        }
        while (op != outrec.getPoints());
    }

    private void updateWindingCount( Edge edge ) {
        LOGGER.entering( DefaultClipper.class.getName(), "updateWindingCount" );

        Edge e = edge.prevInAEL;
        //find the edge of the same polytype that immediately preceeds 'edge' in AEL
        while (e != null && (e.polyTyp != edge.polyTyp || e.windDelta == 0)) {
            e = e.prevInAEL;
        }
        if (e == null) {
            edge.windCnt = edge.windDelta == 0 ? 1 : edge.windDelta;
            edge.windCnt2 = 0;
            e = activeEdges; //ie get ready to calc WindCnt2
        }
        else if (edge.windDelta == 0 && clipType != ClipType.UNION) {
            edge.windCnt = 1;
            edge.windCnt2 = e.windCnt2;
            e = e.nextInAEL; //ie get ready to calc WindCnt2
        }
        else if (edge.isEvenOddFillType( clipFillType, subjFillType )) {
            //EvenOdd filling ...
            if (edge.windDelta == 0) {
                //are we inside a subj polygon ...
                boolean Inside = true;
                Edge e2 = e.prevInAEL;
                while (e2 != null) {
                    if (e2.polyTyp == e.polyTyp && e2.windDelta != 0) {
                        Inside = !Inside;
                    }
                    e2 = e2.prevInAEL;
                }
                edge.windCnt = Inside ? 0 : 1;
            }
            else {
                edge.windCnt = edge.windDelta;
            }
            edge.windCnt2 = e.windCnt2;
            e = e.nextInAEL; //ie get ready to calc WindCnt2
        }
        else {
            //nonZero, Positive or Negative filling ...
            if (e.windCnt * e.windDelta < 0) {
                //prev edge is 'decreasing' WindCount (WC) toward zero
                //so we're outside the previous polygon ...
                if (Math.abs( e.windCnt ) > 1) {
                    //outside prev poly but still inside another.
                    //when reversing direction of prev poly use the same WC
                    if (e.windDelta * edge.windDelta < 0) {
                        edge.windCnt = e.windCnt;
                    }
                    else {
                        edge.windCnt = e.windCnt + edge.windDelta;
                    }
                }
                else {
                    //now outside all polys of same polytype so set own WC ...
                    edge.windCnt = edge.windDelta == 0 ? 1 : edge.windDelta;
                }
            }
            else {
                //prev edge is 'increasing' WindCount (WC) away from zero
                //so we're inside the previous polygon ...
                if (edge.windDelta == 0) {
                    edge.windCnt = e.windCnt < 0 ? e.windCnt - 1 : e.windCnt + 1;
                }
                else if (e.windDelta * edge.windDelta < 0) {
                    edge.windCnt = e.windCnt;
                }
                else {
                    edge.windCnt = e.windCnt + edge.windDelta;
                }
            }
            edge.windCnt2 = e.windCnt2;
            e = e.nextInAEL; //ie get ready to calc WindCnt2
        }

        //update WindCnt2 ...
        if (edge.isEvenOddAltFillType( clipFillType, subjFillType )) {
            //EvenOdd filling ...
            while (e != edge) {
                if (e.windDelta != 0) {
                    edge.windCnt2 = edge.windCnt2 == 0 ? 1 : 0;
                }
                e = e.nextInAEL;
            }
        }
        else {
            //nonZero, Positive or Negative filling ...
            while (e != edge) {
                edge.windCnt2 += e.windDelta;
                e = e.nextInAEL;
            }
        }
    }

} //end Clipper

