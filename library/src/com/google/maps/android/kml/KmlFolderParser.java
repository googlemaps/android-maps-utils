package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlFolderParser {

    private static final String PROPERTY_TAG_REGEX = "name|description|visibility|open";

    private ArrayList<KmlFolder> mContainers;

    private XmlPullParser mParser;

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String STYLE_START_TAG = "Style";

    private final static String FOLDER_START_TAG = "Folder";

    public KmlFolderParser(XmlPullParser parser) {
        mParser = parser;
        mContainers = new  ArrayList<KmlFolder>();
    }

    /**
     * Creates a new folder and adds this to an ArrayList of folders
     */

    public void createContainer() throws XmlPullParserException, IOException {
        KmlFolder folder = new KmlFolder();
        assignFolderProperties(folder);
        mContainers.add(folder);
    }

    /**
     * Takes a parser and assigns variables to a Folder instances
     * @param kmlFolder Folder to assign variables to
     */
    public void assignFolderProperties(KmlFolder kmlFolder)
            throws XmlPullParserException, IOException {
        mParser.next();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Folder"))) {
            System.out.println(mParser.getText());
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(FOLDER_START_TAG)) {
                    KmlFolder container = new KmlFolder();
                    kmlFolder.addChildContainer(container);
                    assignFolderProperties(container);
                } else if (mParser.getName().matches(PROPERTY_TAG_REGEX)) {
                    kmlFolder.setProperty(mParser.getName(), mParser.nextText());
                }  else if (mParser.getName().equals(STYLE_START_TAG)) {
                    KmlStyleParser styleParser = new KmlStyleParser(mParser);
                    styleParser.createStyle();
                    kmlFolder.setStyles(styleParser.getStyles());
                }  else if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    KmlPlacemarkParser placemarkParser = new KmlPlacemarkParser(mParser);
                    placemarkParser.createPlacemark();
                    kmlFolder.setPlacemark(placemarkParser.getPlacemark(), null);
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * @return List of containers
     */
    public ArrayList<KmlFolder> getContainers() {
        return mContainers;
    }


}