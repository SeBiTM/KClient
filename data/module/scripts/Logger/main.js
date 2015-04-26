var App = (new function() {
    this.chatLog = new javax.swing.JTextArea();
    this.cardLog = new javax.swing.JTextArea();
    this.frame = new javax.swing.JFrame("Logger");
        
    this.onAppStart = function () {
        var tabbedPane = new Packages.kclient.ui.TabbedPane();
        tabbedPane.addTab("Chat-Server", new javax.swing.JScrollPane(App.chatLog));
        tabbedPane.addTab("Card-Server", new javax.swing.JScrollPane(App.cardLog));
        
        App.frame.setSize(800, 600);
        App.frame.add(tabbedPane);
        App.frame.setDefaultCloseOperation(2);
        App.frame.setVisible(true);
    };
    this.onAppStop = function () {
        Logger.info('Test');
        App.frame.dispose(); 
    };
    
    this.onPacketReceived = function (packet) {
        App.addChatLog('<<', packet);
        return packet;
    };
    this.onPacketSent = function (packet) {
        App.addChatLog('>>', packet);
        return packet;
    };
    
    this.onNodeReceived = function (connection, node) {
        App.addCardLog('<<', node, connection);
        return node;
    };
    this.onNodeSent = function (connection, node) {
        App.addCardLog('>>', node, connection);
        return node;
    };
    
    this.addChatLog = function (type, packet) {
        var plainPacket = Packages.kclient.tools.HexTool.toString(packet);
        var hexPacket = Packages.kclient.tools.HexTool.toHexArray(packet);
        var node = '';
        if (packet.substring(0, 1) === ':' || packet.substring(0, 1) === 'q') {
            var genNode = groupChat.getBaseNode().read(packet, 2);
            node = '\n     ' + genNode.getName() + '\n     ' + genNode.toString();
        }
        App.chatLog.append('[' + type + '] ' + plainPacket + '\n     ' + hexPacket + node + '\n\n');
    };
    
    this.addCardLog = function (type, node, c) {
        App.cardLog.append('[' + type + '] ' + node.getName() + ' @ ' + c.getType() + '\n     ' + node.toString() + '\n\n');
    };
}());