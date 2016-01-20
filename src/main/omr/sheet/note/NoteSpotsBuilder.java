//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                 N o t e S p o t s B u i l d e r                                //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Herve Bitteur and others 2000-2016. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet.note;

import omr.glyph.Glyph;
import omr.glyph.GlyphFactory;
import omr.glyph.Symbol.Group;

import omr.run.RunTable;

import omr.sheet.Picture;
import omr.sheet.Sheet;
import omr.sheet.SystemInfo;
import omr.sheet.SystemManager;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class {@code NoteSpotsBuilder} builds spot glyphs meant to guide note retrieval.
 *
 * @author Hervé Bitteur
 */
public class NoteSpotsBuilder
{
    //~ Instance fields ----------------------------------------------------------------------------

    /** Related sheet. */
    private final Sheet sheet;

    /** Spot glyphs, per system. */
    Map<SystemInfo, List<Glyph>> glyphMap = new HashMap<SystemInfo, List<Glyph>>();

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new {@code NoteSpotsBuilder} object.
     *
     * @param sheet the related sheet
     */
    public NoteSpotsBuilder (Sheet sheet)
    {
        this.sheet = sheet;
    }

    //~ Methods ------------------------------------------------------------------------------------
    //----------//
    // getSpots //
    //----------//
    /**
     * Retrieve the glyphs out of buffer runs.
     *
     * @return the map of spot glyphs per system
     */
    public Map<SystemInfo, List<Glyph>> getSpots ()
    {
        RunTable noteRuns = sheet.getPicture().getTable(Picture.TableKey.HEAD_SPOTS);
        List<Glyph> spots = GlyphFactory.buildGlyphs(noteRuns, new Point(0, 0), Group.HEAD_SPOT);

        // Dispose the runTable
        sheet.getPicture().removeTable(Picture.TableKey.HEAD_SPOTS);

        // Dispatch spots per system(s)
        return dispatchSheetSpots(spots);
    }

    //--------------------//
    // dispatchSheetSpots //
    //--------------------//
    /**
     * Dispatch sheet spots according to their containing system(s),
     * and keeping only those within system width.
     *
     * @param spots the spots to dispatch
     */
    private Map<SystemInfo, List<Glyph>> dispatchSheetSpots (List<Glyph> spots)
    {
        Map<SystemInfo, List<Glyph>> spotMap = new TreeMap<SystemInfo, List<Glyph>>();

        List<SystemInfo> relevants = new ArrayList<SystemInfo>();
        SystemManager systemManager = sheet.getSystemManager();

        for (Glyph spot : spots) {
            Point center = spot.getCentroid();
            systemManager.getSystemsOf(center, relevants);

            for (SystemInfo system : relevants) {
                // Check glyph is within system abscissa boundaries
                if ((center.x >= system.getLeft()) && (center.x <= system.getRight())) {
                    List<Glyph> list = spotMap.get(system);

                    if (list == null) {
                        spotMap.put(system, list = new ArrayList<Glyph>());
                    }

                    spot.addGroup(Group.HEAD_SPOT); // Needed
                    list.add(spot);
                }
            }
        }

        return spotMap;
    }
}