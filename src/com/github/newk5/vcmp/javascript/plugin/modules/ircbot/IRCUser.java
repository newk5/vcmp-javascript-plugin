

package com.github.newk5.vcmp.javascript.plugin.modules.ircbot;


public class IRCUser {

    private String nick;
    private boolean op;
    private boolean hop;
    private boolean sop;
    private boolean vop;
    private boolean owner;

    public IRCUser() {
    }

    public IRCUser(String nick, boolean op, boolean hop, boolean sop, boolean vop, boolean owner) {
        this.nick = nick;
        this.op = op;
        this.hop = hop;
        this.sop = sop;
        this.vop = vop;
        this.owner = owner;
    }

    private String getLevel() {
        if (op) {
            return "op";
        }
        if (hop) {
            return "hop";
        }
        if (sop) {
            return "sop";
        }
        if (vop) {
            return "vop";
        }
        if (owner) {
            return "owner";
        }
        return "";
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public boolean isHop() {
        return hop;
    }

    public void setHop(boolean hop) {
        this.hop = hop;
    }

    public boolean isSop() {
        return sop;
    }

    public void setSop(boolean sop) {
        this.sop = sop;
    }

    public boolean isVop() {
        return vop;
    }

    public void setVop(boolean vop) {
        this.vop = vop;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

}
