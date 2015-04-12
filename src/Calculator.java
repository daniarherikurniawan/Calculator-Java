import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Calculator {
	// Stack to save each state when we make branch
	private Stack<StateRecorder> mySaveStack = new Stack<StateRecorder>();
	
	// Stack of PDA that contains terminate symbol and nonterminate symbol
	private Stack<String> myStack = new Stack<String>();
	
	// String of element input at time process
	private String currInputElmt = new String();
	
	// String of Input from user
	private String myInput;
	
	// Index of current character at input string 
	private int currIndex;
	
	// Sign if we have make branch at state P -> ( P )
	private boolean makeBranch1 ;
	
	// Sign if we have make branch at state P -> P O P
	private boolean makeBranch2 ;
	
	// Sign if we have make branch at state P -> N
	private boolean makeBranch3 ;
	
	// Mark if we are in state P -> ( P )
	private boolean Mark1;
	
	// Mark if we are in state P -> P O P
	private boolean Mark2;
	
	// Mark if we are in state P -> N
	private boolean Mark3 ;
	
	// Variable to store our result after validate the input
	private boolean Valid;
	
	// Save result of counting process
	private String result;
	
	// Contains Input that will be processed
	Stack<String> myStackInput = new Stack();
	
	// Array that contains Input that will be processed
	Vector<String> myArrayInput = new Vector();
	
	//Symbol for PDA and CFG
	String Symbol_P;
	String Symbol_OB;
	String Symbol_CB;
	String Symbol_O;
	String Symbol_N;
	String Symbol_Epsilon;
	
	// Constructor
	Calculator(){
		currIndex = 0;
		Valid = false;
		result = "Undefined";
		makeBranch1 = false; Mark1 = false;
		makeBranch2 = false; Mark2 = false;
		makeBranch3 = false; Mark3 = false;
		Symbol_P = "P";
		Symbol_OB = "OB";
		Symbol_CB = "CB";
		Symbol_O = "O";
		Symbol_N = "N";
		Symbol_Epsilon = "E";
		myStack.push("P");
		System.out.println("==Calculator==");
	}
	
	// Calculate StrInput to get the result of calculation process
	public String CalculatorEngine(String StrInput){	
		PDA M = new PDA();
		myInput = StrInput;
		myInput = myInput.replaceAll("\\s+","");
		startValidate();
		try{
			if(Valid)
				StartCalculate();
			else
				result = "Input not valid !";
		}catch(Exception e){
			result = "Input not valid !";
		}
		printString("The result is : "+result);
		M.convertPDAtoCFG();
                return result;
                
        }
	
	// start to validate input from user
	private void startValidate(){
		boolean popped = true;
		currInputElmt = getNextInput();
		try{
			addStatetoStack();
			do{
				Mark1 = false;
				Mark2 = false;
				Mark3 = false;
				if(!makeBranch1 ){
					Mark1 = true;
					MakeStack_B_P_B();
					updateParentStack();
				} else if(!makeBranch2){
					Mark2 = true;
					MakeStack_P_O_P();
					updateParentStack();
				}else if(!makeBranch3){
					Mark3 = true;
					MakeStack_N();
					updateParentStack();
				};
				if(makeBranch1 && makeBranch2 &&makeBranch3){
					popStateFromStack();
					//("Every branches failed!");
					backToPrevState();
				}
				if(!IsTopTerminator()){
					while (!IsTopTerminator()&& !myStack.empty()){
						popTopStack();
					}
					addStatetoStack();
				}else {
					addStatetoStack();
				}
			}while ( getNumberOfRemainInput() != 0);
			Valid = true;
		}catch(Exception e){
			Valid = false;
		}
	}
	
	// Calculate the valid input
	private void StartCalculate(){
		createArrayOfInput();
		int i = 0;
		while(i < myArrayInput.size()){
			String CurrInput = myArrayInput.elementAt(i);
			
			String TypeInput = getTypeInput(CurrInput);
			switch(TypeInput){
				case "O" 	: 	processInputOperator(CurrInput);
								break;
				case "(" 	: 	processInputOpenBracket(CurrInput);
								break;
				case ")" 	: 	processInputCloseBracket();
								break;
				case "N" 	: 	processInputNumber(CurrInput);
								break;
				default : printString(TypeInput+"Something wrong with the type input!");
			}
			i++;
		}
		while (getNumberOfOperatorAfterBracket() >= 1){
			processAnyOperatorLevel();
		}
		String ResultCalculation = myStackInput.pop();
		result = ResultCalculation ;
	}
	
	// Return true if myString is terminator
	private boolean IsTerminator(String myString){
		return myString.compareTo("P") == 0;
	}
	
	// Return true if String at top of myStack is terminator
	private boolean IsTopTerminator(){
		if(!myStack.empty())
			return IsTerminator(myStack.peek().toString());
		return true;
	}
	
	// print input to CLI 
	private void printInput(){
		System.out.println("Input : "+ myInput);
	}
	
	// Will take input from file input.txt
	private void getinputCalculationFromFile()throws FileNotFoundException{
		Scanner inFile = new Scanner(new File("InputPDA.txt"));
		myInput = inFile.nextLine();
		myInput = myInput.replaceAll("\\s+","");
	    inFile.close();
	}
	
	// Will take string input from Command Line
	public void getInputFromCMD(){
		Scanner scan = new Scanner(System.in);
		System.out.print("Input your calculation : ");
		myInput = scan.nextLine();
		scan.close();
	}
	
        public String getmyinput(){
            return myInput;
        }
        
        private void getOutput()throws FileNotFoundException{
		Scanner inFile = new Scanner(new File("OutputCFG.txt"));
		myInput = inFile.nextLine();
		myInput = myInput.replaceAll("\\s+","");
	    inFile.close();
	}
        
	// return true if Symbol is an integer
	private boolean IsElmtNumber(char Symbol){
		return (Symbol != '+' && Symbol != '-' && Symbol != '*' && 
				Symbol != '/' && Symbol != '(' && Symbol != ')');
	}
	
	// return number of remain input that not yet processed or popped
	private int getNumberOfRemainInput(){
		int SaveCurrIndex = currIndex;
		int Number = 0;
		while(currIndex != myInput.length()){
			Number ++;
			incCurrIndex();
		}
		currIndex = SaveCurrIndex;
		return Number;
	}
	
	// Use to increment the current index
	private void incCurrIndex(){
		boolean stop = false;
		int index = currIndex;
		boolean prevIsANumber = IsPrevIndexANumber(index);
		boolean prevIsABracket = IsPrevIndexABracket(index);
		do {
			if(!IsElmtNumber(myInput.charAt(index))&& prevIsANumber ){
				//Will be operator
				stop = true;
			}else if(!IsElmtNumber(myInput.charAt(index))&& prevIsABracket ){
				//Will be operator
				stop = true;
			}else if( myInput.charAt(index)==')' || myInput.charAt(index)=='(' ){
				//Will be a bracket
				stop = true;
			}else if (myInput.charAt(index)=='+' ){
				// will be plus operator
				stop = true;
			}else if ( (myInput.charAt(index)=='-' && !prevIsANumber)
					||  !prevIsANumber){
				//Will be a number
				if(index == myInput.length()-1)
					stop = true;
				else if(!IsElmtNumber(myInput.charAt(index + 1)))
					//Bound to the next operator 
					stop = true;
			}
			index++;
		}while(!stop);
		currIndex = index;
	}
	
	// return true if previous index of input is an integer
	private boolean IsPrevIndexANumber(int Index){
		if(Index==0){
			return false;
		}else{
			if(IsElmtNumber(myInput.charAt(Index-1)))
				return true;
			else
				//empty input assumed as operator
				return false;
		}
	}
	
	// return true if previous index of input is a bracket
	private boolean IsPrevIndexABracket(int Index){
		if(Index==0){
			return false;
		}else{
			if(myInput.charAt(Index-1)==')')
				return true;
			else
				return false;
		}
	}
	
	// return the next of element input that will be processed
	private String getNextInput(){
		boolean stop = false;
		int index = currIndex;
		String Elmt = new String();
		boolean prevIsANumber = IsPrevIndexANumber(index);
		boolean prevIsABracket = IsPrevIndexABracket(index);
		do {
			if(!IsElmtNumber(myInput.charAt(index))&& prevIsANumber ){
				//Will be operator
				Elmt += myInput.charAt(index);
				stop = true;
			}else if(!IsElmtNumber(myInput.charAt(index))&& prevIsABracket ){
				//Will be operator
				Elmt += myInput.charAt(index);
				stop = true;
			}else if( myInput.charAt(index)==')' || myInput.charAt(index)=='(' ){
				//Will be a bracket
				Elmt += myInput.charAt(index);
				stop = true;
			}else if (myInput.charAt(index)=='+' ){
				// will be plus operator
				Elmt += myInput.charAt(index);
				stop = true;
			}else if( (myInput.charAt(index)=='-' && !prevIsANumber)
					||  !prevIsANumber){
				//Will be a number
				Elmt += myInput.charAt(index);
				if(index == myInput.length()-1)
					stop = true;
				else if(!IsElmtNumber(myInput.charAt(index + 1)))
					//Bound to the next operator 
					stop = true;
			}
			index++;
		}while(!stop);
		return Elmt;
	}
	
	// make each branching sign to false
	private void initializeMakeBranchToFalse(){
		makeBranch1 = false;
		makeBranch2 = false;
		makeBranch3 = false;
	}
	
	// change stack to state P -> ( P )
	private void MakeStack_B_P_B(){
		initializeMakeBranchToFalse();
		myStack.pop();// delete P
		myStack.push(")");
		myStack.push("P");
		myStack.push("(");
		//So, Top of Stack became : ( , P , )
	}
	
	// change stack to state P -> N
	private void MakeStack_N(){
		initializeMakeBranchToFalse();
		myStack.pop();// delete P
		myStack.push("N");
		//So, Top of Stack became : N 
	}
	
	// change stack to state P -> P O P
	private void MakeStack_P_O_P(){
		initializeMakeBranchToFalse();
		myStack.pop();// delete P
		myStack.push("P");
		myStack.push("O");
		myStack.push("P");
		//So, Top of Stack became : P , O , P 
	}
	
	// return top of myStack
	private String getTopStack(){
		return myStack.peek().toString();
	}
	
	// return type of current element input
	private String getTypeInput(String Elmt){
		if(IsOperator(Elmt))
			return "O";
		if(IsNumber(Elmt))
			return "N";
		if(IsBracket(Elmt))
			return Elmt;
		System.out.println("Input isn't valid!");
		return "Error";
	}
	
	// save currrent Stack to mySaveStack
	private void addStatetoStack(){
		Stack temp = new Stack();
		temp = (Stack)myStack.clone();
		
		StateRecorder StateRec = new StateRecorder(temp,currIndex,makeBranch1,
				makeBranch2, makeBranch3);
		mySaveStack.push(StateRec);
		if(getNumberOfRemainInput() < myStack.size()){
			//("Failed to add stack!");
			updateParentStack();
			popStateFromStack();// delete STack
			peekStateFromStack();
		}
	}
	
	// update branching sign of parent stack that we have saved
	private void updateParentStack(){
		StateRecorder StateRec1;
		if(mySaveStack.size() >= 1){
			StateRec1 = (StateRecorder)mySaveStack.pop();
			StateRec1.SaveBool1 = Mark1 || StateRec1.SaveBool1;
			StateRec1.SaveBool2 = Mark2 || StateRec1.SaveBool2;
			StateRec1.SaveBool3 = Mark3 || StateRec1.SaveBool3;
			mySaveStack.push(StateRec1);
		}
	}
	
	// return previous saved stack to be current stack with popping the SaveStack
	private void popStateFromStack(){
		StateRecorder StateRec;
		StateRec = (StateRecorder)mySaveStack.pop();
		myStack = (Stack)StateRec.SaveState.clone();
		currIndex = StateRec.SaveIndex;
		makeBranch1 = StateRec.SaveBool1;
		makeBranch2 = StateRec.SaveBool2;
		makeBranch3 = StateRec.SaveBool3;
	}
	
	// return previous saved stack to be current stack with peek the SaveStack
	private void peekStateFromStack(){
		StateRecorder StateRec;
		StateRec = (StateRecorder)mySaveStack.peek();
		myStack = (Stack)StateRec.SaveState.clone();
		currIndex = StateRec.SaveIndex;
		makeBranch1 = StateRec.SaveBool1;
		makeBranch2 = StateRec.SaveBool2;
		makeBranch3 = StateRec.SaveBool3;
	}
	
	// print condition of Save Stack
	private void printStateofStack(){
		Stack<StateRecorder> mytempStack = new Stack<StateRecorder>();
		mytempStack = (Stack) mySaveStack.clone();
		System.out.println("\n======================================");
		System.out.println("Jumlah Stack ada : "+mytempStack.size());
		printStack();
		int x = 0;
		while(!mytempStack.empty()){
			StateRecorder StateRec;
			StateRec = (StateRecorder)mytempStack.pop();
			Stack Temp = new Stack();
			Temp = (Stack)StateRec.SaveState.clone();
			System.out.print(StateRec.SaveIndex+" :  ");
			while(!Temp.empty()){
				System.out.print("  "+Temp.pop().toString());
			}
			System.out.print("     "+StateRec.SaveBool1+" "+
			StateRec.SaveBool2+" "+StateRec.SaveBool3);
			System.out.print(" \n");
		}
		System.out.println("======================================\n");
	}
	
	// pop top stack when the top is not terminate symbol and match with PDA grammar
	private void popTopStack(){
		String typeElmt = getTypeInput(currInputElmt);
		String ElmtTop = getTopStack();
		if(  ElmtTop.compareTo(typeElmt) == 0){
			myStack.pop(); // Popping stack\
			incCurrIndex();
			if(getNumberOfRemainInput() != 0){
				currInputElmt = getNextInput();
				typeElmt = getTypeInput(currInputElmt);
			}
			if(myStack.empty() && getNumberOfRemainInput()!=0){
				backToPrevState();
			}
		} else {
			// not matching
			backToPrevState();
		}
	}
	
	// Back to previous state of stack that we have saved before
	private void backToPrevState(){
		updateParentStack();
		popStateFromStack();// delete STack
		currInputElmt = getNextInput();
	}
	
	// print input to CLI to make sure that our type checker is valid
	private void splitInputByType(){
		int index = currIndex;
		while(myInput.length() != currIndex){
			String S = getNextInput();
			String T = getTypeInput(S);
			incCurrIndex();
			printString(S+"  "+T);
		}
		currIndex = index;
	}

	// print any string to CLI
	private void printString(String myString){
		System.out.println(myString);
	}
	
	// print the information of myStack or current stack to CLI
	private void printStack(){
		Stack Temp = new Stack();
		Temp = (Stack)myStack.clone();
		System.out.print("Current Stack : ");
		while(!Temp.empty()){
			System.out.print("  "+Temp.pop().toString());
		}
		System.out.println("");
	}
	
	// return true if Top of my current stack is "P"
	private boolean IsTopP(){
		String Elmt = new String(myStack.peek().toString());
		return Elmt.compareTo("P") == 0 ;
	}
	
	// return true if myString can be convert to an integer
	private boolean IsNumber(String myString){
		if(myString.charAt(0)== '-' && myString.length() > 1){
			//Top element is a negative number
			return true;
		}else if(!IsTerminator(myString) && !IsOperator(myString) 
				&& !IsBracket(myString)){
			return true;
		}
		return false;
	}
	
	// return true if Top of my current stack is a number
	private boolean IsTopNumber(){
		String Top = new String(myStack.peek().toString());
		return IsNumber(Top);
		
	}
	
	// return true if myString is an operator
	private boolean IsOperator(String myString){
		return myString.compareTo("/") == 0 || myString.compareTo("*") == 0 ||
				myString.compareTo("-") == 0 || myString.compareTo("+") == 0;
	}
	
	// return true if Top of my current stack is a Operator
	private boolean IsTopOperator(){
		String Elmt = new String(myStack.peek().toString());
		return IsOperator(Elmt);
	}
	
	// return true if myString is a bracket
	private boolean IsBracket(String myString){
		return myString.compareTo(")") == 0 || myString.compareTo("(") == 0;
	}
	
	// return true if at top of current stack is a bracket
	private boolean IsTopBracket(){
		String Elmt = new String(myStack.peek().toString());
		return IsBracket(Elmt);
	}
	
	// transform the string input to be array
	private void createArrayOfInput(){
		int SaveCurrIndex = currIndex;
		currIndex = 0;;
		while(getNumberOfRemainInput()!= 0){
			myArrayInput.addElement(getNextInput());
			incCurrIndex();
		}
		currIndex = SaveCurrIndex;
	}
	
	// get number of operator that should be processed at that time
	private int getNumberOfOperatorAfterBracket(){
		int i = myStackInput.size()-1;// get Last Index
		boolean find = false;
		int Number = 0;
		while(!find && i >= 0){
			String CurrInput = myStackInput.elementAt(i);
			if(IsOperator(CurrInput))
				Number++;
			if(CurrInput.compareTo(")")==0 ||
					CurrInput.compareTo("(")==0){
				find = true;
				return Number;
			}
			i--;
		}
		return Number; // There isn't any bracket
	}
	
	// return level of operator
	private int getLevelOperator(String Operator){
		if(Operator.compareTo("-") == 0 || Operator.compareTo("+") == 0)
			return 1; // we assumed that + and - is level 1
		else
			return 2; // we assumed that / and * is level 2
	}
	
	// return true if current operator have same level with the previous operator
	private boolean IsSameLevelOperator(String CurrOperator){
		Stack<String> TempStack = new Stack();
		TempStack =(Stack) myStackInput.clone();
		TempStack.pop();
		String prevOperator = TempStack.pop();
		if(getLevelOperator(CurrOperator)==getLevelOperator(prevOperator))
			return true;
		else
			return false;
	}
	
	// parse String that representate a number to be float
	private float getFloatFromString(String str){
		if(str.charAt(0)=='-'){
			str = str.replace("-", "");
			float f = Float.parseFloat(str);
			return f*(-1);
		}else{
			float f = Float.parseFloat(str);
			return f;
		}
		
	}
	
	// process calculation that contains operator level two
	private void processOperatorLevelTwo(){
		String Number1 = myStackInput.pop();
		String Operator = myStackInput.pop();
		String Number2 = myStackInput.pop();
		float N1 = getFloatFromString(Number1);
		float N2 = getFloatFromString(Number2);
		if(Operator.compareTo("*") == 0){
			float Result = N2 * N1;
			myStackInput.push(Float.toString(Result));
		}else{
			float Result = N2 / N1;
			myStackInput.push(Float.toString(Result));
		}
	}
	
	// process calculation that contains operator level one
	private void processOperatorLevelOne(){
		String Number1 = myStackInput.pop();
		String Operator = myStackInput.pop();
		String Number2 = myStackInput.pop();
		float N1 = getFloatFromString(Number1);
		float N2 = getFloatFromString(Number2);
		if(Operator.compareTo("-") == 0){
			float Result = N2 - N1;
			myStackInput.push(Float.toString(Result));
		}else{
			float Result = N2 + N1;
			myStackInput.push(Float.toString(Result));
		}
	}
	
	// handle if the current arrive input is an operator
	private void processInputOperator(String CurrInput){
		if(getNumberOfOperatorAfterBracket() >= 1){
			int LevelOfCurrOperator = getLevelOperator(CurrInput);
			if(LevelOfCurrOperator == 1){
				// compare if prev operator has same level with current operator
				if(IsSameLevelOperator(CurrInput)){
					processOperatorLevelOne();
					myStackInput.push(CurrInput);
				}else{
					//different level operator
					processOperatorLevelTwo();
					myStackInput.push(CurrInput);
				}
			}else{
				// Current operator is level 2 
				if(IsSameLevelOperator(CurrInput)){
					processOperatorLevelTwo();
					myStackInput.push(CurrInput);
				}else{
					//different level operator
					myStackInput.push(CurrInput);
				}
			}
		}else{
			myStackInput.push(CurrInput); //push on stack input
		}
	}
	
	// handle if the current arrive input is an open bracket
	private void processInputOpenBracket(String CurrInput){
		myStackInput.push(CurrInput); //push on stack input
	}
	
	// process calculation that contains any level of operator
	private void processAnyOperatorLevel(){
		String CurrOperator = getClosestOperator();
		if(getLevelOperator(CurrOperator)==1)
			processOperatorLevelOne();
		else
			processOperatorLevelTwo();
	}
	
	// handle if the current arrive input is a close bracket
	private void processInputCloseBracket(){
		while(getNumberOfOperatorAfterBracket() >= 1){
			// It should be only one operator when we will push close bracket
			processAnyOperatorLevel();
		}
		String SaveNumber = myStackInput.pop(); //push on stack input
		myStackInput.pop(); //remove open bracket
		myStackInput.push(SaveNumber);
	}
	
	// return string that contains previous operator that have been saved before at stack
	private String getClosestOperator(){
		Stack<String> TempStack = new Stack();
		TempStack =(Stack) myStackInput.clone();
		String Str = new String("Error!");
		while(!TempStack.empty()){
			Str = TempStack.pop();
			if(IsOperator(Str)){
				return Str;
			}
		}
		return Str;
	}
	
	// print to CLI condition of stackInput at current time of process
	private void printStackInput(){
		Stack<String> TempStack = new Stack();
		TempStack =(Stack) myStackInput.clone();
		printString("\n======================");
		System.out.println("Input stack contains "+TempStack.size()+" elements :");
		while(!TempStack.empty()){
			String ElmtStack = TempStack.pop();
			System.out.print(ElmtStack+ "  ");
		}
		printString("\n======================");
		printString("");
	}
	
	// handle if the current arrive input is a Number
	private void processInputNumber(String CurrInput){
		myStackInput.push(CurrInput); //push on stack input
		if(getNumberOfOperatorAfterBracket() >= 1){
			String CurrOperator = getClosestOperator();
			if(getLevelOperator(CurrOperator)== 2){
				processOperatorLevelTwo();
			}				
		}
	}	
}

