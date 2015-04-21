var App = (new function() {
    var log = new javax.swing.JTextArea();
    
    this.onAppStart = function() {
        var scrollPane = new javax.swing.JScrollPane(log);
        
        var frame = new javax.swing.JFrame("Logger");
        frame.setSize(800, 600);
       // frame.add(scrollPane);
       // frame.setDeafultCloseOperation(2);
        frame.setVisible(true);
    };
    
    
}());