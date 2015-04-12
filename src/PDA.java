import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Vector;


public class PDA {
	Vector <FirstTuple> vecOfPDAFirst = new Vector();
	Vector <SecondTuple> vecOfPDASecond = new Vector();
	Vector <String> vecFirst = new Vector();
	Vector <String> vecOfProduct = new Vector();
	Writer writer = null;
	String TerminalSymbol;
	String baseStack;
	public PDA(){
		baseStack = "undefined";
		TerminalSymbol = "undefined";
	}
	
	public int getNumberOfTransitionFunc(){
		return vecOfPDAFirst.size();
	}
	
	// Make translation from PDA to CFG
	public void convertPDAtoCFG(){
		try {
			getInputPDAFromText();
			int numberTransition = getNumberOfTransitionFunc() - 1;
			while(numberTransition >= 0){
				vecOfPDAFirst.elementAt(numberTransition).parseInputtoElmts();
				vecOfPDASecond.elementAt(numberTransition).parseInputtoElmts();
				numberTransition--;
			}			
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream("OutputCFG.txt"), "utf-8"));
			startConvertingtoFile();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Text unavailable !");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startConvertingtoFile(){
	    int i = 0;
	    try {
	    	writeCFGSpecs();
	    	writeProductResult();
	    	
		    while (i < getNumberOfTransitionFunc()){
		    	FirstTuple One = vecOfPDAFirst.elementAt(i);
		    	SecondTuple Two = vecOfPDASecond.elementAt(i);
		    	
		    	if(Two.IsElmt2Epsilon()){
		    		handleEpsilonElmt(One,Two);
		    	}else{
		    		handleNotEpsilonElmt(One,Two);
		    	}
		    	
		    	if(i!=getNumberOfTransitionFunc()-1)
		    		writer.write("\n");
		    	i++;	
		    }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeProductResult(){
		try {
			int NumberOfFunc = vecOfProduct.size();
			int i = 0, j = 0;
			writer.append("P : \n");
			FirstTuple One = null;
			while (i < NumberOfFunc){
				writer.write("\t"+vecOfProduct.elementAt(i)+"\n");
		    	i++;	
			    	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeCFGSpecs(){
		createFirstTuple();
		try {
			int NumberOfFunc = vecFirst.size();
			int i = 0;
			writer.append("L = ({S, ");
			while (i < vecFirst.size()){
			    	writer.write(vecFirst.elementAt(i));
			    	if(i<NumberOfFunc-1)
			    		writer.write(", ");
			    	else 
			    		writer.write("}, ");
			    	i++;	
			}
			writer.append("{"+TerminalSymbol+"}, P");
			
			writer.append(", ["+vecOfPDAFirst.elementAt(i).Elmt1+
	    			","+baseStack+","+vecOfPDAFirst.elementAt(i).Elmt1+"])\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createFirstTuple(){
		int i = 0;
		int NumberOfFunc = vecOfPDAFirst.size();
		while (i < NumberOfFunc){
	    	FirstTuple One = vecOfPDAFirst.elementAt(i);
	    	SecondTuple Two = vecOfPDASecond.elementAt(i);
	    	String temp = "["+One.Elmt1+","+One.Elmt3+","+Two.Elmt1+"]";
    		if(!vecFirst.contains(temp)){
    			vecFirst.add(temp);
    			int j = 0;
    			while (j < TerminalSymbol.length()){
    				vecOfProduct.add(temp+" -> "+TerminalSymbol.charAt(j));
    				j +=2;
    			}
    		}
	    	i++;	 	
		}
	}
	
	private void writeFirstTuple(FirstTuple One,SecondTuple Two){
    	try {
    		String temp = "["+One.Elmt1+","+One.Elmt3+","+Two.Elmt1+"]";
    		if(!vecFirst.contains(temp))
    			vecFirst.add(temp);
			writer.append(temp);
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void handleNotEpsilonElmt(FirstTuple One,SecondTuple Two){
		try {
			int numberOfElmt2 = Two.Elmt2.length();
			writeFirstTuple(One,Two);
			writer.append(" -> ");
			for(int i = 0 ; i < numberOfElmt2 ; i++){
				writer.append('[');
		    	writer.append(One.Elmt1+","+Two.Elmt2.charAt(i)+","+Two.Elmt1);
		    	writer.append(']');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void handleEpsilonElmt(FirstTuple One,SecondTuple Two){
		try {
			writeFirstTuple(One,Two);
			writer.append(" -> ");
			writer.append(One.Elmt2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getBaseStack(String Line){
		int IdxLastComma=Line.length()-1;
		while(Line.charAt(IdxLastComma)!=',')
			IdxLastComma--;
		return (String) Line.substring(IdxLastComma-1,IdxLastComma);
	}
	
	public String getTerminalSymbol(String Line){
		int IdxFirstBracket = 0;
		int IdxSecondBracket = Line.length()-1;
		while(Line.charAt(IdxFirstBracket)!=',')
			IdxFirstBracket++;
		while(Line.charAt(IdxSecondBracket)!='d')
			IdxSecondBracket--;
		return (String) Line.subSequence(IdxFirstBracket+2,IdxSecondBracket-2);
	}
	
	// Will take input PDA from file InputPDA.txt
	private void getInputPDAFromText()throws FileNotFoundException{
	    BufferedReader br = new BufferedReader(new FileReader("InputPDA.txt"));
	    String line;
	    try {
	    	int i=0;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if(i>0){
					FirstTuple First = new FirstTuple();
					SecondTuple Second = new SecondTuple();
					line = line.replaceAll("\\s+","");
					String[] tokens = line.split("\\=");
					First.Input = tokens[0];
					Second.Input = tokens[1];
					vecOfPDAFirst.add(First);
					vecOfPDASecond.add(Second);
				}else{
					baseStack = getBaseStack(line);
					TerminalSymbol = getTerminalSymbol(line);
				}
				i++;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Text unavailable !");
		}
	    
	}
}
