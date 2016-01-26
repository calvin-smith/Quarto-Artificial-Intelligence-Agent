import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


public class MonteCarloSearch {

	static QuartoBoard originalBoard, workingBoard;
	int numIterations = 50000;
	int agentPlayerNumber = 0;
	boolean critical;
	List <Node> availableChoices = new ArrayList <Node> ();
	static Node submittedAction = new Node();
	List <Node> pieceList;
	Node found;
	static double depthLimit = 2;
	PrintWriter writer;	
	TreeMap<Integer, Double> pieceVal = new TreeMap<Integer, Double>();
	
	/**
	 * {@link getMCS} is the primary method which controls the Monte Carlo Search flow.
	 * This method is overloaded to allow a single method name to be called from the {@link pieceSelectionAlgorithm} and {@link moveSelectionAlgorithm}.
	 * @param copyBoard
	 * @param pieceID
	 * @return
	 */
	public int[] getMCS (QuartoBoard copyBoard, int pieceID) {
		
        agentPlayerNumber = 0;
		
		//availableChoices will hold all possible choices from this point on
		availableChoices = new ArrayList <Node>();
		
		//Iterate through board and find the changes that occurred last turn based on knowing the pieceID only
		for (int column = 0; column < copyBoard.getNumberOfColumns(); column++) {
			for (int row = copyBoard.getNumberOfRows(); row > 0; row--) {
				if (copyBoard.isSpaceTaken(row, column) && 
				   (copyBoard.getPieceOnPosition(row, column).getPieceID() == submittedAction.pieceID)) {

					submittedAction.move[0] = row;
					submittedAction.move[1] = column;
					
				}
			}	
		}
		
		//Instantiate our boards
		originalBoard = new QuartoBoard(copyBoard);
		workingBoard = new QuartoBoard(originalBoard);

		//Go through the availableChoices list and fill all possible moves for the given pieceID
		generateMissingActions(pieceID);
		

		int counter = 0; 
		Random randomGenerator = new Random();
		//Loop through each Node in availableChoices and run a random game on it until numIterations is hit
		
		while (counter <= 300000) {
		
	 	 	int index = randomGenerator.nextInt(availableChoices.size());
					
			recurThroughNextGame(availableChoices.get(index), 2);

				//recurThroughNextGame(e,(int)Math.floor(depthLimit));
				counter++;
			if (critical) {
					
				critical = false;
				return found.move; 
				
			}	
		}
		
		
		//sort the final availableChoices by lowest winDepth
		Collections.sort(availableChoices);
		
		//submittedAction contains the piece and move for this turn
		submittedAction.move = availableChoices.get(0).move;
		submittedAction.pieceID = pieceID;
		
		//Increment the depthLimit each turn to expand the Monte Carlo Search
		depthLimit += 0.4;
		
		//Return the selected move
		return submittedAction.move;
		
	}
	


	public int getMCS (QuartoBoard copyBoard) {
		
		agentPlayerNumber=1;
		
		//availableChoices will hold all possible choices from this point on
		availableChoices = new ArrayList <Node>();
		
		//Instantiate our boards
		originalBoard = new QuartoBoard(copyBoard);
		workingBoard = new QuartoBoard(originalBoard);
		
		//agentPlayerNumber ^= 1;
		generateMissingActions (-1);
		
		//Group pieces into a pieceList
 		pieceList = new ArrayList <Node> ();
		for (int piece = 0; piece < originalBoard.getNumberOfPieces(); piece++) {
			if (!originalBoard.isPieceOnBoard(piece)) {
				
				int nextPiece = originalBoard.chooseNextPieceNotPlayed(piece);
	 			Node newPiece = new Node();
	 			newPiece.pieceID = nextPiece;
	 			pieceList.add(newPiece);
	 			
			}
 		}
		

		//Random randomGenerator = new Random();
		//Loop through each Node in availableChoices and run a random game on it until numIterations is hit
 		
		for (int j = 1; j < 10; j++) {
			for (int x = 0; x < 4; x++) {
				for (int i = 0; i < (availableChoices.size()-1)/j; i++){
			
					recurThroughNextGame(availableChoices.get(i), 3);
			
				}
			}
			
			Collections.sort(availableChoices);
		
		}

		
		//Iterate through each Node in availableChoices.  If the Node's pieceID exists in pieceList, then apply the lowest score
		//of the two to pieceList's score.  
 		for (Node e : availableChoices) { 		
 			for (Node f : pieceList) {
 			
 				if(f.pieceID == e.pieceID){
 					
 					double bestPath = Math.min(e.winDepth, f.winDepth);
 					f.winDepth = bestPath;
 					
 				}
 			}
 		}
 		
 		//Sort the pieceList by winDepth
 		Collections.sort(pieceList);	
 		
 		//Grab the worst winDepth pieceID and do many random games off of it, then keep the best score.  If the best score
 		//is better than the second worst winDepth pieceID, replace it.  Repeat.
 		//scrubTopChoices();
 		if (!pieceList.isEmpty()) submittedAction.pieceID = pieceList.get(pieceList.size()-1).pieceID;
 		else submittedAction.pieceID = workingBoard.chooseNextPieceNotPlayed();
 		
		depthLimit += 0.4;
		return submittedAction.pieceID;
		
	}
	
	
	public void generateMissingActions (int pieceID) {
		
		for (int row = 0; row < originalBoard.getNumberOfRows(); row++) {
			
			for (int column = 0; column < originalBoard.getNumberOfColumns(); column++) {
				
				if (!originalBoard.isSpaceTaken(row, column)) {
					
					if (pieceID == -1) {
						
						for (int piece = 0; piece < originalBoard.getNumberOfPieces(); piece++) {
							if (!originalBoard.isPieceOnBoard(piece)) {
								
								int nextPiece = originalBoard.chooseNextPieceNotPlayed(piece);
																
					    		//Create a new Node for each individual choice at the current turn
							    Node individualChoice = new Node();
							    
							    //individualChoice.player = agentPlayerNumber;
							    
							    individualChoice.pieceID = nextPiece;
							    
					    		//Add next possible choice to the new Node
								individualChoice.move[0] = row;
								individualChoice.move[1] = column;
				
								//Add Node containing a possible choice to the availableChoices array
								availableChoices.add(individualChoice);
	
							}
						}	
					}
					else if (pieceID != -1){
						
			    		//Create a new Node for each individual choice at the current turn
					    Node individualChoice = new Node();
					    
					    //individualChoice.player = agentPlayerNumber;
					    
					    individualChoice.pieceID = pieceID;
						
			    		//Add next possible choice to the new Node
						individualChoice.move[0] = row;
						individualChoice.move[1] = column;
		
						//Add Node containing a possible choice to the availableChoices array
						availableChoices.add(individualChoice);
						
					}
				}
			}
		}		
	}
	
	
	public void recurThroughNextGame(Node currentNode, int depthIn) {
		
		boolean win = false;
		int depth = depthIn;
		
		playRandomMove(currentNode);
		
		while (depth >= 0) {
			
			boolean found = false;
			Node childNode = new Node();
			int randomPiece = calculateRandomPiece(childNode);
			int[] randomMove = calculateRandomMove(childNode);
			
			//If the game is won
			if (checkIfGameIsWon(workingBoard)) {
				
				win = true;
				break;
				
			}
			
			//If board is full
			else if (workingBoard.checkIfBoardIsFull()) {
				//printBoard(workingBoard, currentNode);
				
				break;

			}

				for (Node e : currentNode.children) {
						
					if (e.move[0] == randomMove[0] && 
						e.move[1] == randomMove[1] &&
						e.pieceID == randomPiece) {
						
						currentNode = e;
						currentNode.visits++;
						found = true;
						break;
						
					}
				}
				if (!found){
					
					childNode.parent = currentNode;	
					currentNode.children.add(childNode);
					childNode.move = randomMove;
					childNode.pieceID = randomPiece;
					//childNode.player = childNode.parent.player;
					//childNode.player ^= 1; //Flip player # from 0 to 1 each generation
					childNode.visits ++;
					currentNode = childNode;
					
				}
				
				depth--;
				
				//Play randomly generated turn (piece and move)
				playRandomMove(currentNode);
				
			}
				
				if (win) {
					//Game win 
					gameEnded(currentNode);
				}
				else{
					workingBoard = new QuartoBoard(originalBoard);
				}
			}

	
	
	public int[] calculateRandomMove (Node nodeIn) {
	
		int[] move = workingBoard.chooseRandomPositionNotPlayed(100);
		return move;
		
	}
	
	public int calculateRandomPiece (Node nodeIn) {
		
		int piece = workingBoard.chooseRandomPieceNotPlayed(100);
		return piece;
		
	}
	
	
	public void playRandomMove (Node nodeIn) {
		
		workingBoard.insertPieceOnBoard(nodeIn.move[0], nodeIn.move[1], nodeIn.pieceID);
	
	}
	
	
	public void gameEnded (Node nodeIn) {

		Node currentNode = nodeIn;
		int terminalDepth = 0;
		
		parentFound:
		while (true) {
			
			if (currentNode.winDepth > terminalDepth) {
				
				currentNode.winDepth = terminalDepth;
				terminalDepth++;
				
				if(currentNode.winDepth == 0 && agentPlayerNumber == 1 && !currentNode.hasParent()){//piece
			 		for (Node e : pieceList) {
			 			if(e.pieceID == currentNode.pieceID){
			 				pieceList.remove(e);
			 				break parentFound;
			 			}
			 		}
			 	}		
				else if(currentNode.winDepth == 0 && agentPlayerNumber == 0 && !currentNode.hasParent()){//move
					
					found = currentNode;
					critical = true;
					break;
				}
				if (!currentNode.hasParent()) break parentFound;
				currentNode = currentNode.parent;
				
			}
			else if(currentNode.winDepth <= terminalDepth){
				
				break;	
			}
		}

		workingBoard = new QuartoBoard(originalBoard);
		
	}
	
//Because tree is no longer static, this is not needed
//	public void pruneTree (List <Node> children) {
//		
//		if (!children.isEmpty()){
//			//agentPlayerNumber = children.get(0).player;
//			
//			for (Node e : children) {
//				
//				e.parent = null;
//				
//			}
//			
//			availableChoices = children;
//		}
//	}

	
	//Code from QuartoSemiRandomAgent and modified
	//loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon(QuartoBoard copyBoard) {

        //loop through rows
        for(int i = 0; i < copyBoard.getNumberOfRows(); i++) {
            //gameIsWon = this.quartoBoard.checkRow(i);
            if (copyBoard.checkRow(i)) {
                return true;
            }

        }
        //loop through columns
        for(int i = 0; i < copyBoard.getNumberOfColumns(); i++) {
            //gameIsWon = this.quartoBoard.checkColumn(i);
            if (copyBoard.checkColumn(i)) {
                return true;
            }

        }

        //check Diagonals
        if (copyBoard.checkDiagonals()) {
            return true;
        }

        return false;
    }
    
	
    
	
}
