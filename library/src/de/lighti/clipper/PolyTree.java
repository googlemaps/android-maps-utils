package de.lighti.clipper;

import java.util.ArrayList;
import java.util.List;

class PolyTree extends PolyNode {
    private final List<PolyNode> allPolys = new ArrayList<PolyNode>();

    public void Clear() {
        allPolys.clear();
        childs.clear();
    }

    public List<PolyNode> getAllPolys() {
        return allPolys;
    }

    public PolyNode getFirst() {
        if (!childs.isEmpty()) {
            return childs.get( 0 );
        }
        else {
            return null;
        }
    }

    public int getTotalSize() {
        int result = allPolys.size();
        //with negative offsets, ignore the hidden outer polygon ...
        if (result > 0 && childs.get( 0 ) != allPolys.get( 0 )) {
            result--;
        }
        return result;

    }

}
