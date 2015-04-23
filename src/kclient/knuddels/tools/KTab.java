package kclient.knuddels.tools;

import java.util.ArrayList;
/**
 *
 * @author PaTrick
 */
public class KTab {
    ArrayList tabs = new ArrayList();
    int showTab = 0;
    String url = "";
    String werbung = ""; 
    String werbung2 = ""; 
    
    public KTab() {
        this(0);
    }
    public KTab(int paramShowTab) {
        this(paramShowTab, "", "", "", "");
    }
    public KTab(int paramShowTab, String _title, String _topRightTitle, String _tabTitle, String msg) {
        this.showTab = paramShowTab;
        this.newTab(_title, _topRightTitle, _tabTitle, _tabTitle, msg);
    }

    public void newTab(String _tabTitle, String _headerTitle, String _message) {
        TabSite tab = new TabSite(((TabSite)tabs.get(0)).title, ((TabSite)tabs.get(0)).topRightTitle, _tabTitle, _headerTitle, _message);
        tabs.add(tab);
    }
    public void newTab(String _title, String _topRightTitle, String _tabTitle, String _headerTitle, String _message) {
        TabSite tab = new TabSite(_title, _topRightTitle, _tabTitle, _headerTitle, _message);
        tabs.add(tab);
    }

    public String getSwitchTab() {
        String switchTab = "";
        StringBuilder sites = new StringBuilder();
        int index = -1;
        for (Object objts : tabs) {
            TabSite ts = (TabSite)objts;
            index++;
            sites.append("°>{switchtab}").append(index).append("<°");
            sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb_tl.png<>").append(url).append("layout/boxb_tc...w_0.xrepeat.gif<° ");
            sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb_tr.png<°");
            sites.append("°>{endtable}<>LEFT<°#°+8003°");
            sites.append("°>{table|4|w1,bgimg;").append(url).append("layout/boxb_cl.gif;").append(url).append("layout/boxb_cc.gif;").append(url).append("layout/boxb_cr.gif|4<>{tc}<°");
            sites.append("°11>RIGHT<°").append(ts.topRightTitle).append("_°+6005° #°+9007°°20>LEFT<+0010°_").append(ts.title).append("_°%00° §°>{endtable}<>LEFT<°#°+8002°");
            sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb_bl-f.png<>").append(url).append("layout/boxb_bc-f...w_0.xrepeat.png<° ");
            sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb_br-f.png<°");
            sites.append("°>{endtable}<>LEFT<°#°+8004°°+9005°_");
            sites.append("°BB12>{table|w60|w4<>").append(url).append("layout/line_l.png<°");
            sites.append(activeTab(index));
            sites.append("°>").append(url).append("layout/line_c...w_0.xrepeat.png<° °+6010>{tc}<>RIGHT<>{noxrep}<°");
         //sites.append("°>").append(url).append("layout/tab_i_l...w_8.mx_-1.png<>").append(url).append("layout/tab_i_c...w_0.xrepeat.png<>_hvisit K-Script.in|http://k-script.in/<>{noxrep}<>").append(url).append("layout/tab_i_r.png<°");
            sites.append("°>").append(url).append("layout/line_r.png<+6010>{endtable}<>LEFT<°_°°# #°>Center<°").append(this.werbung).append("#°>Left<°");
            sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb_tl.png<>").append(url).append("layout/boxb_tc...w_0.xrepeat.gif<° ");
            sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb_tr.png<°°>{endtable}<>LEFT<°#°+8003°");
            sites.append("°>{table|4|w1,bgimg;").append(url).append("layout/boxb_cl.gif;").append(url).append("layout/boxb_cc.gif;").append(url).append("layout/boxb_cr.gif|4<>{tc}<°°16>LEFT<+0010°").append(ts.headerTitle);
            sites.append("_°%007°#  §°>{endtable}<>LEFT<°#°1° # °+8003°");
            sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb-h_l.gif<>").append(url).append("layout/boxb-h_c...w_0.xrepeat.gif<° ");
            sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb-h_r.gif<°°>{endtable}<>LEFT<°#°+8002°");
            sites.append("°>{table|4|w1,bgimg;").append(url).append("layout/box_cl.gif;").append(url).append("layout/box_cc.gif;").append(url).append("layout/box_cr.gif|4<>{tc}<°°14+0010° #######################°+8022°°14°");
            sites.append( "°>JUSTIFY<°").append(ts.message).append("##").append(this.werbung2);
            sites.append("#°+9000°#°+9000°#°%00°§°>{endtable}<>LEFT<°#°+8002°°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/box_bl-f.png<>").append(url).append("layout/box_bc-f...w_0.xrepeat.png<° ");
            sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/box_br-f.png<°°>{endtable}<>LEFT<°#§");
        }
        sites.append("°>{showtab}").append(this.showTab).append("<°");
        switchTab = sites.toString();
        return switchTab;
    }

    private String activeTab(int index) {
        StringBuilder strTabs = new StringBuilder();
        int ind = -1;
        for (Object objts : tabs)
        {
            TabSite ts = (TabSite)objts;
            ind++;
            String strTab = "°>"+this.url+"layout/tab_i_l...w_8.mx_-1.png<>"+this.url+"layout/tab_i_c...w_0.xrepeat.png<>_h" + ts.tabTitle + "|/tp-showtab " + ind + "<>{noxrep}<>"+this.url+"layout/tab_i_r.png<°";
            if (ind == index)
                strTab = "°>"+this.url+"layout/tab_a_l...w_8.mx_-1.png<>"+this.url+"layout/tab_a_c...w_0.xrepeat.png<>_h" + ts.tabTitle + "|/tp-showtab " + ind + "<>{noxrep}<>"+this.url+"layout/tab_a_r.png<°";
            strTabs.append(strTab);
        }
        return strTabs.toString();
    }

    private class TabSite {
        public String title;
        public String topRightTitle;
        public String tabTitle;
        public String headerTitle;
        public String message;
        public Content content;

        public TabSite(String _title, String _topRightTitle, String _tabTitle, String _headerTitle, String _message) {
            this.title = _title;
            this.topRightTitle = _topRightTitle;
            this.tabTitle = _tabTitle;
            this.headerTitle = _headerTitle;
            this.message = _message;
        }

    }

    public class Content{
        public String title;
        public String message;

        public Content(String _1, String _2){
            this.title = _1;
            this.message = _2;
        }

        public String getContent(){
            StringBuilder sites = new StringBuilder();
             sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb_tl.png<>").append(url).append("layout/boxb_tc...w_0.xrepeat.gif<° ");
             sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb_tr.png<°°>{endtable}<>LEFT<°#°+8003°");
             sites.append("°>{table|4|w1,bgimg;").append(url).append("layout/boxb_cl.gif;").append(url).append("layout/boxb_cc.gif;").append(url).append("layout/boxb_cr.gif|4<>{tc}<°°16>LEFT<+0010°").append(title);
             sites.append("_°%007°#  §°>{endtable}<>LEFT<°#°1° # °+8003°");
             sites.append("°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/boxb-h_l.gif<>").append(url).append("layout/boxb-h_c...w_0.xrepeat.gif<° ");
             sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/boxb-h_r.gif<°°>{endtable}<>LEFT<°#°+8002°");
             sites.append("°>{table|4|w1,bgimg;").append(url).append("layout/box_cl.gif;").append(url).append("layout/box_cc.gif;").append(url).append("layout/box_cr.gif|4<>{tc}<°°14+0010° #######################°+8022°°14°");
             sites.append( "°>JUSTIFY<°").append(message);
             sites.append("#°+9000°#°+9000°#°%00°§°>{endtable}<>LEFT<°#°+8002°°1>{table|0|w1|w1<>{tc}<>").append(url).append("layout/box_bl-f.png<>").append(url).append("layout/box_bc-f...w_0.xrepeat.png<° ");
             sites.append("°>{tc}<>RIGHT<>{noxrep}<>").append(url).append("layout/box_br-f.png<°°>{endtable}<>LEFT<°#§");
             return sites.toString();
        }
    }
}
