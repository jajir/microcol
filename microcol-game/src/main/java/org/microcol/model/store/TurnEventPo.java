package org.microcol.model.store;

import java.util.List;

public final class TurnEventPo {

    private String playerName;

    private List<Object> args;

    private String messageKey;

    private boolean solved;

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName
     *            the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the args
     */
    public List<Object> getArgs() {
        return args;
    }

    /**
     * @param args
     *            the args to set
     */
    public void setArgs(List<Object> args) {
        this.args = args;
    }

    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @param messageKey
     *            the messageKey to set
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @return the solved
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * @param solved
     *            the solved to set
     */
    public void setSolved(boolean solved) {
        this.solved = solved;
    }

}
