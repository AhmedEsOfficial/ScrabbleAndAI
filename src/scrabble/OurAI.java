package scrabble;

import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class OurAI implements ScrabbleAI {

    //TODO figure out blank spaces
    //TODO make it so AI can actually choose a two letter move, not only on the first move
    //TODO fix chooseMove()
    //TODO write findThreeTileMove() function

    private static final boolean[] ALL_TILES = {false, true, false, true, false, true, false};
    private GateKeeper gateKeeper;

    public Location curBestDirection;
    public Location curBestLocation;
    public int curBestScore = -1;
    PlayWord curBestPlayWord = null;
    boolean firstMove;
    public Node origin;


    public OurAI() {
        GenerateTreeDictionary();
    }

    public void setGateKeeper(GateKeeper gateKeeper) {// i think this is sufficient
        this.gateKeeper = gateKeeper;
    }


    public ScrabbleMove chooseMove() {//ideally this should run all findMove functions and compare best scores

        return findMoveWithTree();


    }

    public class Node {
        char character;
        boolean canStop;
        ArrayList<Node> children;
        Node parent;

        public Node(char c) {
            children = new ArrayList<Node>();
            character = c;
        }

        public void assignParent(Node p) {
            parent = p;
        }

        public void addChild(Node ch) {
            children.add(ch);
        }

        ArrayList<Node> getChildren() {
            return children;
        }

        public boolean hasNeighbour() {
            if (parent != null) {
                return parent.children.size() > 1;
            }
            return false;
        }

        public Node findChild(char c) {
            for (Node child :
                    children) {
                if (child.character == c) {
                    return child;
                }
            }
            return null;
        }
    }



    public void GenerateTreeDictionary() {
        In file = new In("scrabble/words.txt");
        //treeDict = new Hashtable<Character,Node>();
        origin = new Node('0');
        while (!file.isEmpty()) {
            String s = file.readString();
            Node n = new Node(s.charAt(0));
            n.parent = origin;
            if (origin.findChild(n.character) == null) {
                origin.addChild(n);
            } else {
                n = origin.findChild(n.character);
            }
            //System.out.print(n.character);

            Node child;
            for (int i = 1; i < s.length(); i++) {
                child = new Node(s.charAt(i));
                if (!n.children.contains(child)) {
                    n.addChild(child);
                    child.parent = n;
                    n = child;
                    //System.out.print(n.character);

                } else {
                    for (Node c :
                            n.children) {
                        if (c.character == child.character) {
                            c.parent = n;
                            n = c;
                            //System.out.print(n.character);

                            break;
                        }
                    }

                }


            }
            n.canStop = true;

            //System.out.println();

            //System.out.println(s);
        }
    }


    //for each letter in hand
    //
    //if(headNodes.contains(hand[0])); true
    //findHead(hand[i])
    //for j <  length hand -1
    //if(j != i){
    //if(hand[j] in children):
    //


    //lleho

    String badLetters;

    ScrabbleMove findMoveWithTree(){
        curBestScore = -1;
        ArrayList<Character> hand = gateKeeper.getHand(); // AIs hand
        badLetters = "qjxzwkvfybhmp";
        //traverseWordTree(origin, hand);
        if (!Character.isAlphabetic(gateKeeper.getSquare(Location.CENTER))) {
            firstMove = true;
            traverseWordTree(origin, hand);
        }else{
            firstMove = false;
            findAppropriateMove();
        }

        if(curBestScore > -1){
            System.out.println(hand);
            System.out.println(curBestPlayWord.word);
            return curBestPlayWord;

        }
        boolean[] getRid = new boolean[7];
        Arrays.fill(getRid, false);
        for (int i = 0; i <hand.size() ; i++) {
            if(badLetters.contains(hand.get(i)+"")){
                getRid[i] = true;

            }
        }
        return new ExchangeTiles(getRid);
    }

    void CheckWord(String realWord, Location l, Location d){

        try {
            gateKeeper.verifyLegality(realWord,l,d);
            int s = gateKeeper.score(realWord,l,d);
            //System.out.println(s);
            if(s > curBestScore ){
                curBestScore = s;
                curBestPlayWord = new PlayWord(realWord,l,d);
                curBestLocation = l;
                curBestDirection = d;
                //System.out.println("Best move is "+realWord + " and location is "+ l.getRow() + " , " + l.getColumn());
            }
        } catch (IllegalMoveException e) {
            //e.printStackTrace();
        }


    }

    void traverseWordTree(Node cur, ArrayList<Character> set){

        if(cur.canStop){
            Node v = cur;
            String realWord = "";
            while (v.parent != null) {
                if(v.character == '_'){
                    realWord = 'e' + realWord;
                }else{
                    realWord = (v.character) + realWord;
                    v= v.parent;
                }
            }
            if(!firstMove){
                if(!realWord.equals(curBoardWord)){
                    int offset = realWord.indexOf(curBoardWord);
                    String defaultWord = "";
                    if(offset > -1){
                        defaultWord = realWord.replace(curBoardWord, " ".repeat(curBoardWord.length()));
                        Location startLoc = boardWordToLocation.get(curBoardWord);
                        int horSign, verSign;
                        if(boardWordDir == Location.HORIZONTAL){
                            horSign = 1;
                            verSign = 0;
                        }else{
                            horSign = 0;
                            verSign = 1;
                        }
                        Location wordLoc = new Location(startLoc.getRow() + (offset *verSign), startLoc.getColumn() + (offset *horSign ));
                        Location altWordLoc = new Location(startLoc.getRow() - (offset *verSign), startLoc.getColumn() - (offset *horSign ));
                        CheckWord( defaultWord, wordLoc, boardWordDir);
                        CheckWord( defaultWord, altWordLoc, boardWordDir);
                    }
                }
            }else{
                CheckWord(realWord,Location.CENTER,Location.HORIZONTAL);
                CheckWord(realWord,Location.CENTER,Location.VERTICAL);
            }
        }

        for (int i = 0; i < set.size(); i++) {
            Node child = cur.findChild(set.get(i));
            if( child != null){
                ArrayList<Character> newSet = new ArrayList<Character>();
                for (int k = 0; k <set.size() ; k++){
                    if(k != i){
                        newSet.add(set.get(k));
                    }
                }
                traverseWordTree(child,newSet);
            }
        }

    }

    public Location boardCharLoc;

    public char boardChar;

    public int curRow;
    public int curColumn;
    String curBoardWord;
    HashMap<String ,Location> boardWordToLocation;
    Location boardWordDir;;


    //Iterates through board and returns a list and returns the list of words in columns or rows depending on the direction
    ArrayList<String> getWordsFromBoard(Location dir){
        ArrayList<String> output = new ArrayList<>();
        boardWordToLocation = new HashMap<>();
        Location l = Location.CENTER;
        for (int i = 0; i < Board.WIDTH ; i++) {
            Location letterStartLoc = null;
            boolean assignedStartLoc = false;
            String boardWord = "";
            for (int j = 0; j < Board.WIDTH ; j++) {
                if(dir == Location.HORIZONTAL){
                    l = new Location(i,j);
                }else {
                    l  = new Location(j,i);
                }
                Character charOnBoard = gateKeeper.getSquare(l);
                if(Character.isAlphabetic(gateKeeper.getSquare(l))){
                    if(!assignedStartLoc){
                        letterStartLoc = l;
                        assignedStartLoc = true;
                    }
                    boardWord += charOnBoard;
                }
            }
            if(boardWord.length()>0){
                output.add(boardWord);
                if(letterStartLoc != null){
                    boardWordToLocation.put(boardWord,letterStartLoc);
                }
            }

        }
        return output;
    }
    void findAppropriateMove(){
        for (Location dir : new Location[]{Location.HORIZONTAL, Location.VERTICAL}
             ) {
            for (String s: getWordsFromBoard(dir)
            ) {
                ArrayList<Character> h = gateKeeper.getHand();
                curBoardWord = s;
                boardWordDir = dir;
                for (Character c: s.toCharArray()) {
                    h.add(c);
                }
                traverseWordTree(origin, h);
            }
        }
    }

    private char findBestChoiceForBlank(ArrayList<Location> validStartingSquares, PlayWord bestMove, int bestScore){
        char result = 'Z';
        for(char d = 'A'; d <= 'Z'; d++){
            for (String word : new String[] {d + " ", " " + d}) {
                for(Location a : validStartingSquares){
                    int row = a.getRow();
                    int col = a.getColumn();
                    Location location = new Location(row, col);
                    for (Location direction : new Location[] {Location.HORIZONTAL, Location.VERTICAL}) {
                        try {
                            gateKeeper.verifyLegality(word, location, direction);
                            int score = gateKeeper.score(word, location, direction);
                            if (score > bestScore) {
                                result = d;
                            }
                        } catch (IllegalMoveException e) {}
                    }
                }
            }
        }
        return result;
    }

}