package de.team33.miditor.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Rsrc {
    public static final ImageIcon MAIN_ICON = new ImageIcon(Rsrc.class.getResource("miditor.ico.gif"));
    public static final Icon SAVEICON = new ImageIcon(Rsrc.class.getResource("saveicon.gif"));
    public static final Icon SVASICON = new ImageIcon(Rsrc.class.getResource("svasicon.gif"));
    public static final Icon METRONOM = new ImageIcon(Rsrc.class.getResource("metronom.gif"));
    public static final Icon CHECKICO = new ImageIcon(Rsrc.class.getResource("check.gif"));
    public static final Icon UNCHECKICO = new ImageIcon(Rsrc.class.getResource("uncheck.gif"));
    public static final Icon DC_FWDICON = new ImageIcon(Rsrc.class.getResource("dc_forward.gif"));
    public static final Icon DC_REWICON = new ImageIcon(Rsrc.class.getResource("dc_rewind.gif"));
    public static final Icon DC_STPICON = new ImageIcon(Rsrc.class.getResource("dc_stop.gif"));
    public static final Icon DC_PAUSICON = new ImageIcon(Rsrc.class.getResource("dc_pause.gif"));
    public static final Icon DC_RUNICON = new ImageIcon(Rsrc.class.getResource("dc_run.gif"));
    public static final Icon[] DC_ICONSET;

    static {
        DC_ICONSET = new Icon[]{null, DC_STPICON, DC_PAUSICON, DC_RUNICON};
    }

    public Rsrc() {
    }
}
