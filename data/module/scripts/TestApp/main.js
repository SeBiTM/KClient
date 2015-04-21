var App = (new function() {
    this.onAppStart = function() {
    };
    
    this.chatCommands = {
        testCmd: function (cmd, arg, channel) {
            Logger.get().info(cmd + '|' + arg + '|' + channel);
            groupChat.print(channel, 'Test :D');
            return true;
        }
    };
}());