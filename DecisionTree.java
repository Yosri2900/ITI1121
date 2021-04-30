/**
 * This class enables the construction of a decision tree
 * 
 * @author Mehrdad Sabetzadeh, University of Ottawa
 * @author Guy-Vincent Jourdan, University of Ottawa
 *
 */

public class DecisionTree {

	private static class Node<E> {
		E data;
		Node<E>[] children;

		Node(E data) {
			this.data = data;
		}
	}

	Node<VirtualDataSet> root;

	/**
	 * @param data is the training set (instance of ActualDataSet) over which a
	 *             decision tree is to be built
	 */
	public DecisionTree(ActualDataSet data) {
		if (data == null) {
			throw new NullPointerException("you cant do that man!");
		}
		root = new Node<VirtualDataSet>(data.toVirtual());
		build(root);
	}

	
	/**
	 * The recursive tree building function
	 * 
	 * @param node is the tree node for which a (sub)tree is to be built
	 */
	@SuppressWarnings("unchecked")
	private void build(Node<VirtualDataSet> node) {

		if (node == null || node.data == null) {
			throw new NullPointerException("The node is null");
		}
		//if the number of attributes is less than 1 -> throw an exception
		if(node.data.getNumberOfAttributes() < 1){
			throw new IllegalArgumentException("insufficient amount of attributes");
		}
		//if the number of datapoints is less than 1 -> throw an exception
		if(node.data.getNumberOfDatapoints() < 1){
			throw new IllegalArgumentException("Insufficient number of datapoints");
		}
		//if the number of attributes is equal to 1, do nothing
		if(node.data.getNumberOfAttributes() == 1){
			return;
		}
		//check the values of the last column (i.e. last index of the table)
		if(node.data.attributes[node.data.attributes.length-1].getValues().length <= 1){
			return;
		}
		// check if all the columns have at leat 1 attribute value
		boolean flag = false;
		for(int i = 0; i < node.data.attributes.length-1; i++){
			if(node.data.attributes[i].getValues().length > 1){
				flag = true;
			}
		}
		//if its not the case, then do nothing
		if(!flag){
			return;
		}

		//now, starting task 3.1

		//this holds the gain of the entire dataset
		GainInfoItem[] gain = InformationGainCalculator.calculateAndSortInformationGains(node.data);

		//assumne the first element is the best gain (from the entire dataset)
		GainInfoItem bestGain = gain[0];

		//now store it's type
		AttributeType type = bestGain.getAttributeType();

		//we will need an array of virtualdataset for later
		VirtualDataSet[] Partition;
		//if the type is numeric -> then we know we have to find 2 indexes
		if(type == AttributeType.NUMERIC){
			//we are going to get the index from calling the getAttributeIndex method on the name of the bestGain
			int indexForBestGain = node.data.getAttributeIndex(bestGain.getAttributeName());
			//then we are going to get the attribute corresponding to the index found
			Attribute bestGainAttribute = node.data.getAttribute(indexForBestGain);
			
			//store the attribute values of the the specified attribute 
			String[] values = bestGainAttribute.getValues();
			int index = -1;
			for(int i = 0; i < values.length; i++){
				if(values[i].equals(bestGain.getSplitAt())){
					index = i;
					//if i hits the getspiltAt(), then set the index equal to i
					break;
				}
			}
			//now were are going to fill Partiton array (array of virtualdataset from earlier) by calling the partitionByNumericAttribute()
			Partition = node.data.partitionByNumericAttribute(indexForBestGain, index);
		
			}else{
				//else, we know the array is numeric

				//so we are going to fill the Partition array by calling the partitionByNominalAttribute()
				Partition = node.data.partitionByNominallAttribute(node.data.getAttributeIndex(bestGain.getAttributeName()));
			}
			//the children will have the same length as the Partition
			node.children = new Node[Partition.length];
			
			//now fill the children array to be a new node
			for(int i = 0 ; i < Partition.length; i++){
				node.children[i] = new Node<VirtualDataSet>(Partition[i]);
			}
			//final step, recursively call on the children array 
			for(int k = 0; k < node.children.length ; k++){
				build(node.children[k]);
			}


	}

	@Override
	public String toString() {
		return toString(root, 0);
	}

	/**
	 * The recursive toString function
	 * 
	 * @param node        is the tree node for which an if-else representation is to
	 *                    be derived
	 * @param indentDepth is the number of indenting spaces to be added to the
	 *                    representation
	 * @return an if-else representation of node
	 */
	private String toString(Node<VirtualDataSet> node, int indentDepth) {
		//setup a StringBuilder() called result
		StringBuilder result = new StringBuilder();
		//if the children has no information
        if(node.children == null) {
        	//then append the name of the attribute + get the attribute values[0]. (w.r.s to its indentDepth)
            result.append(createIndent(indentDepth)+node.data.getAttribute(node.data.numAttributes-1).getName() +" = "+node.data.getAttribute(node.data.numAttributes-1).getValues()[0] + "\n");
        }else{
        	//we now know the children has still information
            boolean curlyBraceOpened = false;
            for (int i = 0; i < node.children.length; i++) {
            	//append the indentDepth
                result.append(createIndent(indentDepth));
                //if i is still traversing
                if (i>0) {
                	//append the else
                    result.append("else ");
                }
                //then append if ( + getCondition() + new line
                result.append("if (").append(node.children[i].data.getCondition()).append(") {\n");
                //then recursively append node.children[i], indentDepth+2
                result.append(toString(node.children[i], indentDepth+2));

                //we are now over with putting strings, so put the curlyBrace set to true
                curlyBraceOpened = true;
                if (curlyBraceOpened)
                    result.append(createIndent(indentDepth)+"}\n");
                //apend a } + a new line
            }
        }
        return result.toString();
	}

	/**
	 * @param indentDepth is the depth of the indentation
	 * @return a string containing indentDepth spaces; the returned string (composed
	 *         of only spaces) will be used as a prefix by the recursive toString
	 *         method
	 */
	private static String createIndent(int indentDepth) {
		if (indentDepth <= 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < indentDepth; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws Exception {
	
		StudentInfo.display();

		if (args == null || args.length == 0) {
			System.out.println("Expected a file name as argument!");
			System.out.println("Usage: java DecisionTree <file name>");
			return;
		}

		String strFilename = args[0];

		ActualDataSet data1 = new ActualDataSet(new CSVReader(strFilename));

		DecisionTree dtree1 = new DecisionTree(data1);

		System.out.println(dtree1);

	}
}