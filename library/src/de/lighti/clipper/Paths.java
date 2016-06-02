package de.lighti.clipper;

import java.util.ArrayList;

/**
 * A pure convenience class to avoid writing List<Path> everywhere.
 *
 * @author Tobias Mahlmann
 *
 */
public class Paths extends ArrayList<Path> {

    public static Paths closedPathsFromPolyTree( PolyTree polytree ) {
        final Paths result = new Paths();
        //        result.Capacity = polytree.Total;
        result.addPolyNode( polytree, PolyNode.NodeType.CLOSED );
        return result;
    }

    public static Paths makePolyTreeToPaths( PolyTree polytree ) {

        final Paths result = new Paths();
        //        result.Capacity = polytree.Total;
        result.addPolyNode( polytree, PolyNode.NodeType.ANY );
        return result;
    }

    public static Paths openPathsFromPolyTree( PolyTree polytree ) {
        final Paths result = new Paths();
        //        result.Capacity = polytree.ChildCount;
        for (final PolyNode c : polytree.getChilds()) {
            if (c.isOpen()) {
                result.add( c.getPolygon() );
            }
        }
        return result;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1910552127810480852L;

    public Paths() {
        super();
    }

    public Paths( int initialCapacity ) {
        super( initialCapacity );
    }

    public void addPolyNode( PolyNode polynode, PolyNode.NodeType nt ) {
        boolean match = true;
        switch (nt) {
            case OPEN:
                return;
            case CLOSED:
                match = !polynode.isOpen();
                break;
            default:
                break;
        }

        if (polynode.getPolygon().size() > 0 && match) {
            add( polynode.getPolygon() );
        }
        for (final PolyNode pn : polynode.getChilds()) {
            addPolyNode( pn, nt );
        }
    }

    public Paths cleanPolygons() {
        return cleanPolygons( 1.415 );
    }

    public Paths cleanPolygons( double distance ) {
        final Paths result = new Paths( size() );
        for (int i = 0; i < size(); i++) {
            result.add( get( i ).cleanPolygon( distance ) );
        }
        return result;
    }

    public LongRect getBounds() {

        int i = 0;
        final int cnt = size();
        final LongRect result = new LongRect();
        while (i < cnt && get( i ).isEmpty()) {
            i++;
        }
        if (i == cnt) {
            return result;
        }

        result.left = get( i ).get( 0 ).getX();
        result.right = result.left;
        result.top = get( i ).get( 0 ).getY();
        result.bottom = result.top;
        for (; i < cnt; i++) {
            for (int j = 0; j < get( i ).size(); j++) {
                if (get( i ).get( j ).getX() < result.left) {
                    result.left = get( i ).get( j ).getX();
                }
                else if (get( i ).get( j ).getX() > result.right) {
                    result.right = get( i ).get( j ).getX();
                }
                if (get( i ).get( j ).getY() < result.top) {
                    result.top = get( i ).get( j ).getY();
                }
                else if (get( i ).get( j ).getY() > result.bottom) {
                    result.bottom = get( i ).get( j ).getY();
                }
            }
        }
        return result;
    }

    public void reversePaths() {
        for (final Path poly : this) {
            poly.reverse();
        }
    }

}
