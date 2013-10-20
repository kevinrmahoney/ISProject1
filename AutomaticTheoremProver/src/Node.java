import java.util.ArrayList;


public class Node {
	
	// the defined operations are below, any argument passed as a Node
	// must conform to them
	public static final char AND = '&';				// logical AND 
	public static final char OR = '|';				// logical OR
	public static final char IMPLY = '>';			// logical IMPLICATION (commonly => or ->)
	public static final char DOUBLE_IMPLY = '*';	// logical DOUBLE IMPLICATION (commonly <--> or <==>)
	public static final char NEGATIVE = '-';		// logical NEGATION
	
	public static final char PLEFT = '(';
	public static final char PRIGHT = ')';
	
	private boolean negative;		// if this node is negative
	private Node left;				// left argument of this node
	private Node right;				// right argument of this node
	
	private String root;			// logical operator (&,|,>,*) or argument (predicate)
	
	public Node( String expression ) {
		setup(expression);
	}
	
	public Node(Node node) {
		setup(node.toString());
	}
	
	private void setup(String expression) {
		// if there are parentheses around the entire expression, and the entire expression is negated
		if(encased(expression) && expression.startsWith(NEGATIVE+"")) {
			negative = true;						// make the node negative
			expression = expression.substring(1);	// remove the negative sign
			expression = trim(expression);			// trim the useless parentheses
		}
		// if there is a logical operator in there, parse the arguments
		if(	expression.contains(AND + "") || expression.contains(OR + "") || expression.contains(IMPLY + "")) {
			expression = trim(expression);
			findMiddle(expression);
		} else {	// its a leaf, meaning its an argument itself
			negative = expression.startsWith(NEGATIVE+"");
			root = expression.replace(NEGATIVE + "", "");
			left = null;
			right = null;
		}
	}
	
	/** negate()
	 * negate this expression
	 */
	public void negate() {
		negative = !negative;
	}

	/** trim()
	 * trims useless parentheses (ex: (a&b) == a&b)
	 * @param s  the expression to trim
	 * @return the trimmed expression
	 */
	private String trim(String s) {
		if(encased(s)) {
			return s.substring(1, s.length()-1);
		} else {
			return s;
		}
	}
	
	/** returns if the expression is surrounded by parentheses
	 * 
	 * @param s  check if this is encased
	 * @return true if encased, false otherwise
	 */
	private boolean encased(String s) {
		int countP = 0;		// count of parentheses, 1 for every '(', -1 for every ')'
		int matchedP = -1;  // index where the parentheses end up matching
		if(s.startsWith(NEGATIVE+"")) {  // if it starts with a negative sign, ignore the negative
			s = s.substring(1);
		}
	
		if(s.contains(PLEFT + "")) {	
			
			// increment count for every '(', decrement for every ')'
			// when the parentheses match, break out of the loop
			for(int i = 0; i < s.length(); i++) {
				if(			s.charAt(i) == PLEFT) {
					countP++;
				} else if(	s.charAt(i) == PRIGHT) {
					countP--;
				}
				if(countP == 0) {
					matchedP = i;
					break;
				}
			}
		}
		// if they matched only at the end, and there were an equal number of left and right
		// parentheses, then return true, this is encased
		if(matchedP == s.length()-1 && countP == 0) {
			return true;
		} else if(countP != 0 ) {	// mismatched parentheses if count isnt equal
			throw new IllegalArgumentException("Mismatched parentheses.");
		} else {
			return false;
		}
	}

	/** findMiddle()
	 * finds the "middle" operator, and recursively constructs the axiom's tree
	 * based on what's left of it and right of it.
	 * @param s  the expression
	 */
	private void findMiddle(String s) {
		ArrayList<Integer> operations = new ArrayList<Integer>();
		
		// compile the index of each operator
		for(int i = 0; i < s.length(); i++) {
			if( s.charAt(i) == OR || s.charAt(i) == AND || s.charAt(i) == IMPLY) {
				operations.add(i);
			}
		}
		
		// if there is only one operator, then just branch based off of that
		if(operations.size() == 1) {
			left = new Node(s.substring(0, operations.get(0)));
			right = new Node(s.substring(operations.get(0)+1));
			root = s.charAt(operations.get(0)) + "";
		} else {
			// go through each operator and check if it's the middle
			for(Integer i : operations) {
				if(isMiddle(s, i)) {
					left = new Node(s.substring(0, i));
					right = new Node(s.substring(i+1));
					root = s.charAt(i) + "";
					break;
				}
			}
		}
	}
	
	/** isMiddle()
	 * checks if an operator is the middle operator
	 * @param s  the logical expression
	 * @param i  the index to check
	 * @return true if it is, false otherwise
	 */
	private boolean isMiddle(String s, Integer i) {
		boolean isMiddle = false;
		int countLLP = countChar(s.substring(0,i), PLEFT);	// number of left parentheses in substring left of index i
		int countRLP = countChar(s.substring(0,i), PRIGHT);	// number of right parentheses in substring left of index i
		if(countLLP == countRLP) {
			isMiddle = true;
		}
		return isMiddle;
	}
	
	/** countChar()
	 * count number of occurrences of a character in a string
	 * @param s  string to look for characters in
	 * @param c  the character to compare to
	 * @return count  # of occurrences of c in s
	 */
	private int countChar(String s, char c) {
		int count = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}
	
	/** negativeSign()
	 * 
	 * @return return a "-" if negative, otherwise empty string
	 */
	private String negativeSign() {
		if(negative) {
			return NEGATIVE + "";
		} else {
			return "";
		}
	}
	
	/** isLeaf()
	 * @return return true if there are not left and right nodes, false otherwise
	 */
	public boolean isLeaf() {
		return left == null && right == null;
	}
	
	/** toString()
	 * @return String representation of this node
	 */
	public String toString() {
		if( left != null && right != null) {
			return negativeSign() + "(" + left.toString()  + root + right.toString() + ")";
		} else {
			return negativeSign() + root;
		}
	}
	
	/** convertImply()
	 * apply material implication to this node and to nodes branching from it
	 */
	public void convertImply() {
		if(root.equals(IMPLY + "")) {
			root = OR+"";
			left.negate();
		}
		if(!isLeaf()) {
			left.convertImply();
			right.convertImply();
		}
	}

	/** propogate()
	 * propogate negative signs based on DeMorgan's Laws
	 */
	public void propogate() {
		// if this node is negative and not a leaf
		// use DeMorgan's Law and negate the left and right
		// arguments and turn OR into AND, or AND into OR
		if(negative && !isLeaf()) {
			negate();
			if(root.equals(OR+"")) {
				root = AND+"";
			} else {
				root = OR+"";
			}
			left.negate();
			right.negate();
		}
		// if this is not a leaf, do the same for branching nodes
		if(!isLeaf()) {
			left.propogate();
			right.propogate();
		}
	}

	public boolean applyDistribution() {
		if(!isLeaf() && !left.isLeaf()) {
			if(root.equals(OR+"") && left.root.equals(AND+"")) {
				// apply distribution law
				// (left.left & left.right) | right  			-- BEFORE
				// (left.left | right) & (left.right | right)	-- AFTER
				String LL = left.left.toString();
				String LR = left.right.toString();
				String R = right.toString();

				left.left = new Node(LL);
				left.right = new Node(R);
				right.left = new Node(LR);
				right.right = new Node(R);

				left.root = OR+"";
				right.root = OR+"";
				root = AND+"";
				return false;
			}
		} else if(!isLeaf() && !right.isLeaf())  {
			if(root.equals(OR+"") && right.root.equals(AND+"")) {
				// apply distribution law
				// left | (right.left & right.right)  			-- BEFORE
				// (left | right.left) & (left | right.right)	-- AFTER
				String RL = right.left.toString();
				String RR = right.right.toString();
				String L = left.toString();

				left.left = new Node(L);
				left.right = new Node(RL);
				right.left = new Node(L);
				right.right = new Node(RR);

				left.root = OR+"";
				right.root = OR+"";
				root = AND+"";
				return false;
			}
		}

		if(!isLeaf()) {
			return left.applyDistribution() && right.applyDistribution();
		} else {
			return true;
		}
	}
}
