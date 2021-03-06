/**
 * Copyright (c) 2011, Jordi Cortadella
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package interp;

import parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

/** Class that implements the interpreter of the language. */

public class Interp {

    /** Special functions factory **/
    private FuncFactory funcFactory;

    /** Memory of the virtual machine. */
    private Stack Stack;

    /**
     * Map between function names (keys) and ASTs (values).
     * Each entry of the map stores the root of the AST
     * correponding to the function.
     */
    private HashMap<String,AslTree> FuncName2Tree;

    /** Standard input of the interpreter (System.in). */
    private Scanner stdin;

    /**
     * Stores the line number of the current statement.
     * The line number is used to report runtime errors.
     */
    private int linenumber = -1;

    /** File to write the trace of function calls. */
    private PrintWriter trace = null;

    /** Nested levels of function calls. */
    private int function_nesting = -1;

    /**
     * Constructor of the interpreter. It prepares the main
     * data structures for the execution of the main program.
     */
    public Interp(AslTree T, String tracefile) {
        assert T != null;
        funcFactory = FuncFactory.getinstance();
        MapFunctions(T);  // Creates the table to map function names into AST nodes
        PreProcessAST(T); // Some internal pre-processing ot the AST
        Stack = new Stack(); // Creates the memory of the virtual machine
        // Initializes the standard input of the program
        stdin = new Scanner (new BufferedReader(new InputStreamReader(System.in)));
        if (tracefile != null) {
            try {
                trace = new PrintWriter(new FileWriter(tracefile));
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }
        }
        function_nesting = -1;
    }

    /** Runs the program by calling the main function without parameters. */
    public Data Run() {
        Data result = executeFunction ("main", null);
        if(result==null){
            result = new VoidData();
        }
        return result;
    }

    /** Returns the contents of the stack trace */
    public String getStackTrace() {
        return Stack.getStackTrace(lineNumber());
    }

    /** Returns a summarized contents of the stack trace */
    public String getStackTrace(int nitems) {
        return Stack.getStackTrace(lineNumber(), nitems);
    }

    /**
     * Gathers information from the AST and creates the map from
     * function names to the corresponding AST nodes.
     */
    private void MapFunctions(AslTree T) {
        assert T != null && T.getType() == AslLexer.LIST_FUNCTIONS;
        FuncName2Tree = new HashMap<String,AslTree> ();
        int n = T.getChildCount();
        for (int i = 0; i < n; ++i) {
            AslTree f = T.getChild(i);
            assert f.getType() == AslLexer.FUNC;
            String fname = f.getChild(0).getText();
            if (FuncName2Tree.containsKey(fname)) {
                throw new RuntimeException("Multiple definitions of function " + fname);
            }
            FuncName2Tree.put(fname, f);
        }
    }

    /**
     * Performs some pre-processing on the AST. Basically, it
     * calculates the value of the literals and stores a simpler
     * representation. See AslTree.java for details.
     */
    private void PreProcessAST(AslTree T) {
        if (T == null) return;
        switch(T.getType()) {
            case AslLexer.INT: T.setIntValue(); break;
            case AslLexer.STRING: T.setStringValue(); break;
            case AslLexer.BOOLEAN: T.setBooleanValue(); break;
            default: break;
        }
        int n = T.getChildCount();
        for (int i = 0; i < n; ++i) PreProcessAST(T.getChild(i));
    }

    /**
     * Gets the current line number. In case of a runtime error,
     * it returns the line number of the statement causing the
     * error.
     */
    public int lineNumber() { return linenumber; }

    /** Defines the current line number associated to an AST node. */
    private void setLineNumber(AslTree t) { linenumber = t.getLine();}

    /** Defines the current line number with a specific value */
    private void setLineNumber(int l) { linenumber = l;}

    /**
     * Executes a function.
     * @param funcname The name of the function.
     * @param args The AST node representing the list of arguments of the caller.
     * @return The data returned by the function.
     */
    private Data executeFunction (String funcname, AslTree args) {
        if (funcFactory.contains(funcname)) {
          SpecialFunc sf = funcFactory.getFunction(funcname);
          Data result = sf.call(listArguments(args));
          return result;
        }

        // Get the AST of the function
        AslTree f = FuncName2Tree.get(funcname);
        if (f == null) throw new RuntimeException(" function " + funcname + " not declared");

        // Gather the list of arguments of the caller. This function
        // performs all the checks required for the compatibility of
        // parameters.
        ArrayList<Data> Arg_values = listArguments(f, args);

        // Dumps trace information (function call and arguments)
        if (trace != null) traceFunctionCall(f, Arg_values);

        // List of parameters of the callee
        AslTree p = f.getChild(1);
        int nparam = p.getChildCount(); // Number of parameters

        // Create the activation record in memory
        Stack.pushActivationRecord(funcname, lineNumber());

        // Track line number
        setLineNumber(f);

        // Copy the parameters to the current activation record
        for (int i = 0; i < nparam; ++i) {
            String param_name = p.getChild(i).getText();
            Stack.defineVariable(param_name, Arg_values.get(i));
        }

        // Execute the instructions
        Data result = executeListInstructions (f.getChild(2));

        // If the result is null, then the function returns void
        if (result == null) result = new VoidData();

        // Dumps trace information
        if (trace != null) traceReturn(f, result, Arg_values);

        // Destroy the activation record
        Stack.popActivationRecord();

        return result;
    }

    /**
     * Executes a block of instructions. The block is terminated
     * as soon as an instruction returns a non-null result.
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the block of instructions.
     * @return The data returned by the instructions (null if no return
     * statement has been executed).
     */
    private Data executeListInstructions (AslTree t) {
        assert t != null;
        Data result = null;
        int ninstr = t.getChildCount();
        for (int i = 0; i < ninstr; ++i) {
            result = executeInstruction (t.getChild(i));
            if (result != null) return result;
        }
        return null;
    }

    /**
     * Executes an instruction.
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the instruction.
     * @return The data returned by the instruction. The data will be
     * non-null only if a return statement is executed or a block
     * of instructions executing a return.
     */
    private Data executeInstruction (AslTree t) {
        assert t != null;

        setLineNumber(t);
        Data value; // The returned value

        // A big switch for all type of instructions
        switch (t.getType()) {

            // Assignment
            case AslLexer.ASSIGN: {
                value = evaluateExpression(t.getChild(1));
                if(t.getChild(0).getType() == AslLexer.ACCESS){
                    AslTree subtree = t.getChild(0);
                    Data container = Stack.getVariable(subtree.getChild(0).getText());
                    accessDataAndAssign(subtree, container, value);
                }else{
                    Stack.defineVariable (t.getChild(0).getText(), value);
                    value = Stack.getVariable(t.getChild(0).getText());
                }
                return null;
            }

            // If-then-else
            case AslLexer.IF: {
                value = evaluateExpression(t.getChild(0));
                checkType("Boolean", value);
                BooleanData bool_value = (BooleanData) value;
                if (bool_value.getValue()) return executeListInstructions(t.getChild(1));
                // Is there else statement ?
                if (t.getChildCount() == 3) return executeListInstructions(t.getChild(2));
                return null;
            }

            // While
            case AslLexer.WHILE: {
                BooleanData bool_value;
                while (true) {
                    value = evaluateExpression(t.getChild(0));
                    checkType("Boolean", value);
                    bool_value = (BooleanData) value;
                    if (!bool_value.getValue()) return null;
                    Data r = executeListInstructions(t.getChild(1));
                    if (r != null) return r;
                }
            }

            // Return
            case AslLexer.RETURN: {
                if (t.getChildCount() != 0) {
                    return evaluateExpression(t.getChild(0));
                }
                return new VoidData(); // No expression: returns void data
            }

            // Read statement: reads a variable and raises an exception
            // in case of a format error.
            case AslLexer.READ: {
                String token = null;
                IntegerData val = new IntegerData();
                try {
                    token = stdin.next();
                    val.setValue(Integer.parseInt(token));
                } catch (NumberFormatException ex) {
                    throw new RuntimeException ("Format error when reading a number: " + token);
                }
                Stack.defineVariable (t.getChild(0).getText(), val);
                return null;
            }

            // Write statement: it can write an expression or a string.
            case AslLexer.WRITE: {
                AslTree v = t.getChild(0);
                // Special case for strings
                if (v.getType() == AslLexer.STRING) {
                    System.out.format(v.getStringValue());
                    return null;
                }

                // Write an expression
                System.out.print(String.format(evaluateExpression(v).toString()));
                return null;
            }

            // Write statement: it can write an expression or a string.
            case AslLexer.WRITELN: {
                AslTree v = t.getChild(0);
                // Special case for strings
                if (v.getType() == AslLexer.STRING) {
                    System.out.format(v.getStringValue());
                    System.out.println("");
                    return null;
                }

                // Write an expression
                System.out.print(String.format(evaluateExpression(v).toString()));
                System.out.println("");
                return null;
            }

            // Function call
            case AslLexer.FUNCALL: {
                executeFunction(t.getChild(0).getText(), t.getChild(1));
                return null;
            }

            default: assert false; // Should never happen
        }

        // All possible instructions should have been treated.
        assert false;
        return null;
    }

    /**
     * Evaluates the expression represented in the AST t.
     * @param t The AST of the expression
     * @return The value of the expression.
     */

    private Data evaluateExpression(AslTree t) {
        assert t != null;
        int previous_line = lineNumber();
        setLineNumber(t);
        int type = t.getType();

        Data value = null;
        // Atoms
        switch (type) {
            // A variable
            case AslLexer.ID:
                value = Stack.getVariable(t.getText()).deepClone();
                break;
            // An integer literal
            case AslLexer.INT:
                value = new IntegerData(t.getIntValue());
                break;
            // A Boolean literal
            case AslLexer.BOOLEAN:
                value = new BooleanData(t.getBooleanValue());
                break;
            // A function call. Checks that the function returns a result.
            case AslLexer.FUNCALL:
                value = executeFunction(t.getChild(0).getText(), t.getChild(1));
                assert value != null;
                if (Data.isType("Void", value)) {
                    throw new RuntimeException ("function expected to return a value");
                }
                break;
            // An String
            case AslLexer.STRING:
                value = new StringData(t.getStringValue());
                break;
            case AslLexer.LIST:
                ArrayList<Data> llista = new ArrayList<Data>();
                for(int i=0; i<t.getChildCount(); ++i){
                    Data list_elem = evaluateExpression(t.getChild(i));
                    llista.add(list_elem);
                }
                value = new ListData<Data>(llista);
                break;
            case AslLexer.DICT:
                HashMap<StringData,Data> dict = new HashMap<StringData,Data>();
                StringData col; Data d;
                for(int i = 0; i<t.getChildCount(); i+=2){
                   col = (StringData) evaluateExpression(t.getChild(i));
                   d = evaluateExpression(t.getChild(i+1));
                   dict.put(col,d);
                }
                value = new DictData(dict);
                break;
            case AslLexer.ACCESS:
                Data container = Stack.getVariable(t.getChild(0).getText()).deepClone();
                value = accessData(t,container);
                break;
            case AslLexer.FROM: {
                Data table = Stack.getVariable(t.getChild(0).getText());
                value = evaluateFromActions(table,t.getChild(1));
                break;
            }
            default: break;
        }

        // Retrieve the original line and return
        if (value != null) {
            setLineNumber(previous_line);
            return value;
        }

        // Unary operators
        value = evaluateExpression(t.getChild(0));
        if (t.getChildCount() == 1) {
            switch (type) {
                case AslLexer.PLUS:
                    checkType("Integer", value);
                    break;
                case AslLexer.MINUS:
                    checkType("Integer", value);
                    IntegerData int_val = (IntegerData) value;
                    int_val.setValue(-int_val.getValue());
                    break;
                case AslLexer.NOT:
                    checkType("Boolean", value);
                    BooleanData bool_val = (BooleanData) value;
                    bool_val.setValue(!bool_val.getValue());
                    break;
                default: assert false; // Should never happen
            }
            setLineNumber(previous_line);
            return value;
        }

        // Two operands
        Data value2;
        switch (type) {
            // Relational operators
            case AslLexer.EQUAL:
            case AslLexer.NOT_EQUAL:
            case AslLexer.LT:
            case AslLexer.LE:
            case AslLexer.GT:
            case AslLexer.GE:
                value2 = evaluateExpression(t.getChild(1));
                if (value.getType() != value2.getType()) {
                  throw new RuntimeException ("Incompatible types in relational expression");
                }
                value = value.evaluateRelational(type, value2);
                break;

            // Arithmetic operators
            case AslLexer.PLUS:
            case AslLexer.MINUS:
            case AslLexer.MUL:
            case AslLexer.DIV:
            case AslLexer.MOD:
                value2 = evaluateExpression(t.getChild(1));
                value = value.evaluateArithmetic(type, value2);
                break;

            // Boolean operators
            case AslLexer.AND:
            case AslLexer.OR:
                // The first operand is evaluated, but the second
                // is deferred (lazy, short-circuit evaluation).
                checkType("Boolean", value);
                value = evaluateBoolean(type,(BooleanData)value,t.getChild(1));
                break;

            default: {
              assert false; // Should never happen
            }
        }

        setLineNumber(previous_line);
        return value;
    }

    private Data evaluateContextExpression(TableData table, int row_i, AslTree t) {
        assert t != null;
        int type = t.getType();

        Data value = null;
        // Atoms
        switch (type) {
            case AslLexer.ID: {
                value = Stack.getVariable(t.getText()).deepClone();
                break;
            }
            case AslLexer.INT: {
                value = new IntegerData(t.getIntValue());
                break;
            }
            case AslLexer.BOOLEAN: {
                value = new BooleanData(t.getBooleanValue());
                break;
            }
            case AslLexer.FUNCALL: {
                value = executeFunction(t.getChild(0).getText(), t.getChild(1));
                assert value != null;
                if (Data.isType("Void", value)) {
                    throw new RuntimeException ("function expected to return a value");
                }
                break;
            }
            case AslLexer.STRING:
                value = new StringData(t.getStringValue());
                break;
            case AslLexer.LIST:
                ArrayList<Data> llista = new ArrayList<Data>();
                for(int i=0; i<t.getChildCount(); ++i){
                    Data list_elem = evaluateContextExpression(table, row_i, t.getChild(i));
                    llista.add(list_elem);
                }
                value = new ListData<Data>(llista);
                break;
            case AslLexer.DICT:
                HashMap<StringData,Data> dict = new HashMap<StringData,Data>();
                StringData col; Data d;
                for(int i = 0; i<t.getChildCount(); i+=2){
                   col = (StringData) evaluateContextExpression(table, row_i, t.getChild(i));
                   d = evaluateContextExpression(table, row_i, t.getChild(i+1));
                   dict.put(col,d);
                }
                value = new DictData(dict);
                break;
            case AslLexer.ACCESS: {
                Data container = Stack.getVariable(t.getChild(0).getText()).deepClone();
                value = accessData(t,container);
                break;
            }
            case AslLexer.FROM: {
                Data table_aux = Stack.getVariable(t.getChild(0).getText());
                value = evaluateFromActions(table_aux,t.getChild(1));
                break;
            }

            default: break;
        }

        // Retrieve the original line and return
        if (value != null) {
            return value;
        }

        // Unary operators
        value = evaluateContextExpression(table, row_i, t.getChild(0));
        if (t.getChildCount() == 1) {
            switch (type) {
                case AslLexer.PLUS:
                    checkType("Integer", value);
                    break;
                case AslLexer.MINUS:
                    checkType("Integer", value);
                    IntegerData int_val = (IntegerData) value;
                    int_val.setValue(-int_val.getValue());
                    break;
                case AslLexer.NOT:
                    checkType("Boolean", value);
                    BooleanData bool_val = (BooleanData) value;
                    bool_val.setValue(!bool_val.getValue());
                    break;
                case AslLexer.COLUMN: {
                    Data colData = evaluateExpression(t.getChild(0));
                    value = table.get(row_i, colData);
                    break;
                }
                default: assert false; // Should never happen
            }
            return value;
        }

        // Two operands
        Data value2;
        switch (type) {
            // Relational operators
            case AslLexer.EQUAL:
            case AslLexer.NOT_EQUAL:
            case AslLexer.LT:
            case AslLexer.LE:
            case AslLexer.GT:
            case AslLexer.GE:
                value2 = evaluateContextExpression(table, row_i, t.getChild(1));
                if (value.getType() != value2.getType()) {
                  throw new RuntimeException ("Incompatible types in relational expression");
                }
                value = value.evaluateRelational(type, value2);
                break;

            // Arithmetic operators
            case AslLexer.PLUS:
            case AslLexer.MINUS:
            case AslLexer.MUL:
            case AslLexer.DIV:
            case AslLexer.MOD:
                value2 = evaluateContextExpression(table, row_i, t.getChild(1));
                value = value.evaluateArithmetic(type, value2);
                break;

            // Boolean operators
            case AslLexer.AND:
            case AslLexer.OR:
                // The first operand is evaluated, but the second
                // is deferred (lazy, short-circuit evaluation).
                checkType("Boolean", value);
                value = evaluateContextShortCircuit(table,row_i,type,(BooleanData)value,t.getChild(1));
                break;

            default: {
              assert false; // Should never happen
            }
        }

        return value;
    }


    public Data evaluateFromActions(Data table, AslTree t) {
      assert t.getType() == AslLexer.FROM_ACTIONS;
      assert Data.isType("Table", table);
      int n = t.getChildCount();

      TableData res = new TableData();
      TableData previous = TableData.cast(table);
      for (int i=0; i<n; i++) {
        res = new TableData(previous.getStringDataLabels(),previous.getTypes());
        int type = t.getChild(i).getType();
        for (int j=0; j<previous.height(); j++) {
          boolean b;
          DictData row;
          switch(type) {
            case AslLexer.SELECT: {
                b = evaluateContextBoolean(previous,j,t.getChild(i).getChild(0));
                if (b) res.addRow(previous.get(j));
                break;
            }

            case AslLexer.FILTER: {
                b = !evaluateContextBoolean(previous,j,t.getChild(i).getChild(0));
                if (b) res.addRow(previous.get(j));
                break;
            }

            case AslLexer.UPDATE: {
                Data col, value;
                if (t.getChild(i).getChildCount()<3) {
                  col = evaluateExpression(t.getChild(i).getChild(0));
                  value = evaluateExpression(t.getChild(i).getChild(1));
                  b = true;
                } else {
                  col = evaluateExpression(t.getChild(i).getChild(0));
                  value = evaluateExpression(t.getChild(i).getChild(2));
                  b =evaluateContextBoolean(previous,j,t.getChild(i).getChild(1));
                }
                res.addRow(previous.get(j));
                if (b) res.put(j,StringData.cast(col),value);
                break;
            }

            default: assert false;
          }
        }
        previous = res;
      }
      return res;
    }

    public boolean evaluateContextBoolean(TableData table, int i, AslTree t) {
      Data res = evaluateContextExpression(table, i, t);
      checkType("Boolean", res);
      return BooleanData.cast(res).getValue();
    }

    /**
     * Evaluation of Boolean expressions. This function implements
     * a short-circuit evaluation. The second operand is still a tree
     * and is only evaluated if the value of the expression cannot be
     * determined by the first operand.
     * @param type Type of operator (token).
     * @param v First operand.
     * @param t AST node of the second operand.
     * @return An Boolean data with the value of the expression.
     */
    private Data evaluateBoolean (int type, BooleanData v, AslTree t) {
        // Boolean evaluation with short-circuit

        switch (type) {
            case AslLexer.AND:
                // Short circuit if v is false
                if (!v.getValue()) return v;
                break;

            case AslLexer.OR:
                // Short circuit if v is true
                if (v.getValue()) return v;
                break;

            default: assert false;
        }

        // Return the value of the second expression
        Data aux = evaluateExpression(t);
        checkType("Boolean", aux);
        return (BooleanData) aux;
    }
    private Data evaluateContextShortCircuit (TableData table, int row_i, int type, BooleanData v, AslTree t) {
        // Boolean evaluation with short-circuit

        switch (type) {
            case AslLexer.AND:
                // Short circuit if v is false
                if (!v.getValue()) return v;
                break;

            case AslLexer.OR:
                // Short circuit if v is true
                if (v.getValue()) return v;
                break;

            default: assert false;
        }

        // Return the value of the second expression
        Data aux = evaluateContextExpression(table, row_i, t);
        checkType("Boolean", aux);
        return (BooleanData) aux;
    }

    /** Checks that the data is Type type and raises an exception if it is not. */
    private void checkType (String type, Data b) {
        if (b.getType() != type) {
            throw new RuntimeException ("Expecting " + type + " expression");
        }
    }

    /**
     * Gathers the list of arguments of a function call. It also checks
     * that the arguments are compatible with the parameters. In particular,
     * it checks that the number of parameters is the same and that no
     * expressions are passed as parametres by reference.
     * @param AstF The AST of the callee.
     * @param args The AST of the list of arguments passed by the caller.
     * @return The list of evaluated arguments.
     */

    private ArrayList<Data> listArguments (AslTree AstF, AslTree args) {
        if (args != null) setLineNumber(args);
        AslTree pars = AstF.getChild(1);   // Parameters of the function

        // Create the list of parameters
        ArrayList<Data> Params = new ArrayList<Data> ();
        int n = pars.getChildCount();

        // Check that the number of parameters is the same
        int nargs = (args == null) ? 0 : args.getChildCount();
        if (n != nargs) {
            throw new RuntimeException ("Incorrect number of parameters calling function " +
                                        AstF.getChild(0).getText());
        }

        // Checks the compatibility of the parameters passed by
        // reference and calculates the values and references of
        // the parameters.
        for (int i = 0; i < n; ++i) {
            AslTree p = pars.getChild(i); // Parameters of the callee
            AslTree a = args.getChild(i); // Arguments passed by the caller
            setLineNumber(a);
            if (p.getType() == AslLexer.PVALUE) {
                // Pass by value: evaluate the expression
                Params.add(i,evaluateExpression(a));
            } else {
                // Pass by reference: check that it is a variable
                if (a.getType() != AslLexer.ID) {
                    throw new RuntimeException("Wrong argument for pass by reference");
                }
                // Find the variable and pass the reference
                Data v = Stack.getVariable(a.getText());
                Params.add(i,v);
            }
        }
        return Params;
    }

    private ArrayList<Data> listArguments (AslTree args) {
        // Create the list of parameters
        ArrayList<Data> Params = new ArrayList<Data> ();
        int nargs = (args == null) ? 0 : args.getChildCount();

        // Checks the compatibility of the parameters passed by
        // reference and calculates the values and references of
        // the parameters.
        for (int i = 0; i < nargs; ++i) {
            AslTree a = args.getChild(i); // Arguments passed by the caller
            setLineNumber(a);
            if (a.getType() == AslLexer.ID) {
              // Find the variable and pass the reference
              Data v = Stack.getVariable(a.getText());
              Params.add(i,v);
            } else {
              Params.add(i,evaluateExpression(a));
            }
        }
        return Params;
    }

    private Data accessData(AslTree t, Data container){
        Data value;
        ArrayList<Data> indexes = listArguments(t.getChild(1));
        Data i = indexes.get(0);
        Data j;
        value = container.get(i);
        for(int dims = 1; dims < indexes.size(); ++dims){
            j = indexes.get(dims);
            value = value.get(j);
        }

        return value;
    }

    private void accessDataAndAssign(AslTree t, Data container, Data value){
        Data elem;
        ArrayList<Data> indexes = listArguments(t.getChild(1));
        Data i = indexes.get(0);
        Data j = new VoidData();
        Data parent = container;
        Data great_parent = new VoidData();
        elem = container.get(i);

        for(int dims = 1; dims < indexes.size(); ++dims){
            great_parent = parent;
            parent = elem;
            j = i;
            i = indexes.get(dims);
            elem = elem.get(i);
        }

        if(parent.getType().equals("Table") && elem.getType().equals("Dict")){
            throw new RuntimeException("Cannot replace an entire row from a table");
        }
        if(parent.getType().equals("Dict") && elem.getType().equals("Void")){
            DictData dict = (DictData) parent;
            StringData key = (StringData) i;
            TableData table = (TableData) great_parent;
            IntegerData row = (IntegerData) j;
            if(great_parent.getType().equals("Table")) table.put(row.getValue(),key,value);
            else dict.put(key,value);
        }else{
            elem.setValue(value);
        }
    }

    /**
     * Writes trace information of a function call in the trace file.
     * The information is the name of the function, the value of the
     * parameters and the line number where the function call is produced.
     * @param f AST of the function
     * @param arg_values Values of the parameters passed to the function
     */

    private void traceFunctionCall(AslTree f, ArrayList<Data> arg_values) {
        function_nesting++;
        AslTree params = f.getChild(1);
        int nargs = params.getChildCount();

        for (int i=0; i < function_nesting; ++i) trace.print("|   ");

        // Print function name and parameters
        trace.print(f.getChild(0) + "(");
        for (int i = 0; i < nargs; ++i) {
            if (i > 0) trace.print(", ");
            AslTree p = params.getChild(i);
            if (p.getType() == AslLexer.PREF) trace.print("&");
            trace.print(p.getText() + "=" + arg_values.get(i));
        }
        trace.print(") ");

        if (function_nesting == 0) trace.println("<entry point>");
        else trace.println("<line " + lineNumber() + ">");
    }

    /**
     * Writes the trace information about the return of a function.
     * The information is the value of the returned value and of the
     * variables passed by reference. It also reports the line number
     * of the return.
     * @param f AST of the function
     * @param result The value of the result
     * @param arg_values The value of the parameters passed to the function
     */

    private void traceReturn(AslTree f, Data result, ArrayList<Data> arg_values) {
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");
        function_nesting--;
        trace.print("return");
        if (!Data.isType("Void", result)) trace.print(" " + result);

        // Print the value of arguments passed by reference
        AslTree params = f.getChild(1);
        int nargs = params.getChildCount();
        for (int i = 0; i < nargs; ++i) {
            AslTree p = params.getChild(i);
            if (p.getType() == AslLexer.PVALUE) continue;
            trace.print(", &" + p.getText() + "=" + arg_values.get(i));
        }

        trace.println(" <line " + lineNumber() + ">");
        if (function_nesting < 0) trace.close();
    }
}
