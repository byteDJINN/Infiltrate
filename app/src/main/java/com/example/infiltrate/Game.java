package com.example.infiltrate;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The class used to represent the entire game as a whole.
 */
public class Game {
    /**
     * Enum of the various roles with two attributes: isAlive, isCitizen
     */
    public enum Role {
        INFILTRATOR(Type.LIVING_INFILTRATOR,
                "Chance to murder someone.",-1),
        SPECTRE(Type.DEAD_INFILTRATOR,
                "Chance to decrease the number of turns until someone's next turn.",0.3),
        PHANTOM(Type.DEAD_INFILTRATOR,
                "Chance to randomly change the target of some citizen.",0.3),
        WRAITH(Type.DEAD_INFILTRATOR,
                "Chance to scramble selections of everyone targeting some infiltrator.",0.3),
        CITIZEN(Type.LIVING_CITIZEN,
                "Chance to lynch someone.",-1),
        EXORCIST(Type.LIVING_CITIZEN,
                "Chance to change the role of someone dead.",0.3),
        APPARITION(Type.DEAD_CITIZEN,
                "Chance to decrease number of turns until some dead person's next turn.",0.3),
        GHOST(Type.DEAD_CITIZEN,
                "Chance to reveal someone's role to a random person.",0.3),
        POLTERGEIST(Type.DEAD_CITIZEN,
                "Chance to shuffle someone's turn to a random later point.",0.3),
        UNDEAD(Type.DEAD_CITIZEN,
                "Can speak. Chance to lynch someone.",-1);

        public boolean isAlive;
        public boolean isCitizen;
        public String description;
        public double probability;
        Role(Type t,String desc,double probability) {
            this.isAlive = t.ordinal() == Type.LIVING_CITIZEN.ordinal() ||
                    t.ordinal() == Type.LIVING_INFILTRATOR.ordinal();
            this.isCitizen = t.ordinal() == Type.LIVING_CITIZEN.ordinal() ||
                    t.ordinal() == Type.DEAD_CITIZEN.ordinal();
            this.description = desc;
            this.probability = probability;
        }

        private enum Type {
            LIVING_INFILTRATOR, DEAD_INFILTRATOR,
            LIVING_CITIZEN, DEAD_CITIZEN
        }
    }
    private class Snapshot {
        public int turn; // turn number for that player
        public Game.Role role; // player's role
        public String name; // player name
        public String target; // player's target
        public String message;
        public boolean done; // whether or not the turn already finished

        Snapshot(String name, int turn, Role role) {
            this.name = name;
            this.turn = turn;
            this.role = role;
            this.done = false;
            this.message = role.description;
            this.target = "";
        }
    }

    private ArrayList<Snapshot> data; // All the snapshots that have happened in order
    private Pair<Pair<ArrayList<Role>,ArrayList<Role>>,Pair<ArrayList<Role>,ArrayList<Role>>>
            rolePool; // Pool of roles when assigning players

    private String winningSide;

    /**
     * GameState constructor.
     * @param playerNames: an ArrayList of the players' names.
     * @param roleRestrictions: an ArrayList of Pairs, the key being each role, and the value being
     *                        an array of two integers, the first being the minimum and the second
     *                        being the maximum number of that role which can exist in the game.
     */
    Game(ArrayList<String> playerNames, ArrayList<Pair<Role,int[]>> roleRestrictions) {
        data = new ArrayList<>();
        rolePool = new Pair<>(new Pair<>(new ArrayList<>(),new ArrayList<>()),new Pair<>(new ArrayList<>(),new ArrayList<>()));

        Collections.shuffle(playerNames);
        Collections.shuffle(roleRestrictions);

        // Set players to be minimum number of living roles
        for (Pair<Role,int[]> rolePair : roleRestrictions) {
            if (rolePair.first.isAlive) {
                for (int i = 0; i < rolePair.second[0]; i++) { // Minimum values
                    data.add(new Snapshot(playerNames.get(0), 0, rolePair.first));
                    playerNames.remove(0);
                }
            }
        }
        // Generate the rolePool using minimums of dead roles
        for (Pair<Role,int[]> rolePair : roleRestrictions) {
            if (!rolePair.first.isAlive) {
                for (int i=0;i<rolePair.second[0];i++) { // Minimum values
                    getRolePool(rolePair.first.isAlive, rolePair.first.isCitizen).add(rolePair.first);
                }
            }
        }
        // Shuffle the dead roles minimums
        Collections.shuffle(getRolePool(false,true));
        Collections.shuffle(getRolePool(false,false));
        // Append onto rolePool more roles based on maximums
        while (roleRestrictions.size() > 0) {
            Pair<Role,int[]> rolePair = roleRestrictions.get(getRandom(0,roleRestrictions.size()));
            // use maximum as a counter for how many left to add
            if (rolePair.second[0] != 0) {
                rolePair.second[1] -= rolePair.second[0];
                rolePair.second[0] = 0;
            }
            if (rolePair.second[1] > 0) {
                rolePair.second[1] -= 1;
                getRolePool(rolePair.first.isAlive,rolePair.first.isCitizen).add(rolePair.first);
            }
            if (rolePair.second[1] == 0) {
                roleRestrictions.remove(rolePair);
            }

        }
        // Set extra players
        while (playerNames.size() > 0) {
            Role role = drawRole(true,getRandomBoolean(0.5));
            data.add(new Snapshot(playerNames.get(0), 0, role));
            playerNames.remove(0);
        }

    }

    public String getCurrentPlayerName() { return getCurrentSnapshot().name; }
    public Role getCurrentPlayerRole() { return getCurrentSnapshot().role; }
    public String getCurrentPlayerMessage() { return getCurrentSnapshot().message; }
    public String getWinningSide() { return this.winningSide; }

    /**
     * Sets the most recent selection of the player to be the target.
     * @param playerName - The user who is doing the targeting.
     * @param targetName - The user who has been targeted.
     */
    public void setSelection(String playerName, String targetName) {
        data.get(getPlayerSnapshotIndex(playerName)).target = targetName;
    }

    /**
     * Gets the names of all players that have been playing.
     * @return - an ArrayList of player names
     */
    public ArrayList<String> getPlayerNames() {
        ArrayList<String> playerNames = new ArrayList<String>();
        for (Snapshot s : data) {
            if (!playerNames.contains(s.name)) {
                playerNames.add(s.name);
            }
        }
        return playerNames;
    }

    public Role getPlayerRole(String name) {
        for (int i=data.size()-1;i>=0;i--) {
            if (data.get(i).name.equals(name)) {
                return data.get(i).role;
            }
        }
        throw new AssertionError("Something Broke!");
    }

    /**
     * Checks if the game is over or not. Changes the value of variable winningSide.
     * @return - True if game is over.
     */
    public boolean checkWin() {
        winningSide = "NOBODY";
        boolean cLost = true;
        boolean iLost = true;
        for (String p : getPlayerNames()) {
            if (getPlayerRole(p).isAlive&&getPlayerRole(p).isCitizen) cLost = false;
            if (getPlayerRole(p).isAlive&&!getPlayerRole(p).isCitizen) iLost = false;
        }
        winningSide = cLost ? "INFILTRATORS" : iLost ? "CITIZENS" : "NOBODY";
        return cLost != iLost;
    }

    // HELPER METHODS //

    /**
     * Gets the snapshot of the current turn that is happening.
     * @return - The snapshot of the current turn.
     */
    private Snapshot getCurrentSnapshot() {
        for (Snapshot s : data) {
            if (!s.done) {
                return s;
            }

        }
        throw new AssertionError("Something Broke!");
    }

    /**
     * Returns the index of the most recent snapshot of a player.
     * @param name - name of player.
     * @return - the index of the snapshot.
     */
    private int getPlayerSnapshotIndex(String name) {
        for (int i=data.size()-1;i>=0;i--) {
            if (data.get(i).name.equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Something Broke!");
    }

    /**
     * Generates a probability based boolean.
     * @param probability - Probability of true.
     * @return - A boolean.
     */
    private boolean getRandomBoolean(double probability){
        return Math.random() < probability;
    }

    private int getRandom(int min,int max) {
        return min + (int)(Math.random() * (max - min));
    }

    private void killPlayer(String name) {
        // TODO: dead player gets to have one extra turn after being killed (bug)
        Snapshot ss = data.get(getPlayerSnapshotIndex(name));
        ss.message += "\nYou have died. You now have a new role.";
        ss.role = drawRole(false,ss.role.isCitizen);
        ss.message += "\n"+ss.role.description;
    }

    private ArrayList<Role> getRolePool(boolean isAlive, boolean isCitizen) {
        ArrayList<Role> specificRolePool;
        if (isCitizen) {
            if (isAlive) {
                specificRolePool = rolePool.first.first;
            } else {
                specificRolePool = rolePool.first.second;
            }
        } else {
            if (isAlive) {
                specificRolePool = rolePool.second.first;
            } else {
                specificRolePool = rolePool.second.second;
            }
        }
        return specificRolePool;
    }

    private Role drawRole(boolean isAlive, boolean isCitizen) {
        Role role = getRolePool(isAlive,isCitizen).get(0);
        getRolePool(isAlive, isCitizen).remove(0);
        return role;
    }



    // ROLE-DEPENDENT METHODS //

    /**
     * Gets the possible players that a given player can legally target.
     * @param playerName - The name of a player.
     * @return - The other players which the given player can legally target.
     */
    public ArrayList<String> getLegalTargets(String playerName) {
        ArrayList<String> legalTargets = new ArrayList<String>();
        switch (getPlayerRole(playerName))  {
            case POLTERGEIST:
            case GHOST:
            case SPECTRE:
                for (String p : getPlayerNames()) {
                    legalTargets.add(p);
                }
                break;
            case UNDEAD:
            case CITIZEN:
                for (String p : getPlayerNames()) {
                    if (getPlayerRole(p).isAlive) { legalTargets.add(p); }
                }
                break;
            case INFILTRATOR:
                for (String p : getPlayerNames()) {
                    if (getPlayerRole(p).isAlive && getPlayerRole(p).isCitizen) { legalTargets.add(p); }
                }
                break;
            case APPARITION:
            case EXORCIST:
                for (String p : getPlayerNames()) {
                    if (!getPlayerRole(p).isAlive) { legalTargets.add(p); }
                }
                break;
            case PHANTOM:
                for (String p : getPlayerNames()) {
                    if (getPlayerRole(p).isCitizen) { legalTargets.add(p); }
                }
                break;
            case WRAITH:
                for (String p : getPlayerNames()) {
                    if (!getPlayerRole(p).isCitizen) { legalTargets.add(p); }
                }
                break;
            default:
                // if it gets to here, code for a role is missing
        }

        legalTargets.add("");
        return legalTargets;
    }

    /**
     * Changes any internal variables and makes any changes as a result of the
     * selection of the current player. Sets up the fields to be ready for the
     * next player's turn.
     */
    public void doTurn() {

        Snapshot ss = getCurrentSnapshot();
        String successMessage = ""; // added to the current player's messages

        if (!ss.target.equals("")) {
            switch (ss.role) {
                case UNDEAD:
                case CITIZEN:
                    // Calculate number of citizens
                    int c = 0;
                    for (String name : getPlayerNames()) {
                        if (getPlayerRole(name) == Role.CITIZEN) c++;
                    }

                    if (getRandomBoolean(1.0/(c+1))) {
                        killPlayer(ss.target);
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;
                case INFILTRATOR: {
                    // Calculate number of infiltrators
                    int i = 0;
                    for (String name : getPlayerNames()) {
                        if (getPlayerRole(name) == Role.INFILTRATOR) i++;
                    }
                    if (getRandomBoolean(1.0/(i+1))) {
                        killPlayer(ss.target);
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;}
                case POLTERGEIST:
                    if (getRandomBoolean(Role.POLTERGEIST.probability)) {
                        int ti = getPlayerSnapshotIndex(ss.target);
                        data.add(getRandom(ti + 1, data.size() - 1), data.get(ti));
                        data.remove(ti);
                        successMessage = "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;
                case GHOST:
                    if (getRandomBoolean(Role.GHOST.probability)) {
                        ArrayList<String> names = getPlayerNames();
                        int i = getPlayerSnapshotIndex(names.get(getRandom(0, names.size() - 1)));
                        data.get(i).message += "\n" + ss.target + " has role " + getPlayerRole(ss.target).name();
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;
                case APPARITION:
                    if (getRandomBoolean(Role.APPARITION.probability)) {
                        int ti = getPlayerSnapshotIndex(ss.target);
                        int ci = getPlayerSnapshotIndex(ss.name);
                        int i = getRandom(0,ti-ci);
                        data.add(i+ci,data.get(ti));
                        data.remove(ti);
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;
                case SPECTRE:
                    if (getRandomBoolean(Role.SPECTRE.probability)) {
                        int ti = getPlayerSnapshotIndex(ss.target);
                        int ci = getPlayerSnapshotIndex(ss.name);
                        int i = getRandom(0,ti-ci);
                        data.add(i+ci,data.get(ti));
                        data.remove(ti);
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;

                case EXORCIST:
                    if (getRandomBoolean(Role.EXORCIST.probability)) {
                        int i = getPlayerSnapshotIndex(ss.target);
                        Role prevRole = data.get(i).role;
                        data.get(i).role = drawRole(false,data.get(i).role.isCitizen);
                        getRolePool(false,data.get(i).role.isCitizen).add(prevRole);

                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;
                case PHANTOM:
                    if (getRandomBoolean(Role.PHANTOM.probability)) {
                        int i = getPlayerSnapshotIndex(ss.target);
                        data.get(i).target = getLegalTargets(data.get(i).name).get(getRandom(0,getLegalTargets(data.get(i).name).size()-1));
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;

                case WRAITH:
                    if (getRandomBoolean(Role.WRAITH.probability)) {
                        for (String name : getPlayerNames()) {
                            int index = getPlayerSnapshotIndex(name);
                            if (data.get(index).target.equals(ss.target)) {
                                data.get(index).target = getLegalTargets(data.get(index).name).get(getRandom(0,getLegalTargets(data.get(index).name).size()-1));
                            }
                        }
                        successMessage += "\nSuccess on "+ss.target;
                    } else { successMessage = "\nFailure on "+ss.target; }
                    break;

                default:
                    // if it gets to here, code for a role is missing
                    break;

            }
        }

        // Add the new empty snapshot for this player's next turn
        Snapshot nextss = new Snapshot(ss.name,ss.turn+1,ss.role);
        nextss.message += successMessage;
        data.add(nextss);
        ss.done = true; // Registers that the turn is over

    }








}


