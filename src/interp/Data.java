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

/**
 * Class to represent data in the interpreter.
 * Each data item has a type and a value. The type can be integer
 * or Boolean. Each operation asserts that the operands have the
 * appropriate types.
 * All the arithmetic and Boolean operations are calculated in-place,
 * i.e., the result is stored in the same data.
 * The type VOID is used to represent void values on function returns.
 */

import parser.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Data {
    /** Types of data */
    public enum Type {VOID, BOOLEAN, INTEGER, LIST, DICT, TABLE;}

    /** Type of data*/
    private Type type;

    /** Value of the data */
    private int value;
    
    private ArrayList<Data> list;
    
    private Hashtable<String, Data> dict;
    
    private ArrayList<Hashtable<String, Data>> table;

    /** Constructor for integers */
    Data(int v) { type = Type.INTEGER; value = v; }

    /** Constructor for Booleans */
    Data(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }
    
    /** Constructor for Lists */
    Data(ArrayList<Data> b) { type = Type.LIST; list = b; }
    
    /** Constructor for Dictionaries */
    Data(Hashtable<String, Data> b) { type = Type.DICT; dict = b; }
    
    /** Constructor for Tables */
    Data(ArrayList<Hashtable<String, Data>> b) { type = Type.TABLE; table = b; }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    /** Copy constructor */
    Data(Data d) { 
        type = d.type;
        value = d.value; 
        list = d.list.clone();
        dict = d.dict.clone();
        table = d.table.clone();
    }

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isInteger() { return type == Type.INTEGER; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }
    
    /** Indicates whether the data is list */
    public boolean isList(){ return type == Type.LIST;}
    
    /** Indicates whether the data is dictionary */
    public boolean isDict() { return type == Type.DICT; }
    
    /** Indicates whether the data is table */
    public boolean isTable(){ return type == Type.TABLE; }

    /**
     * Gets the value of an integer data. The method asserts that
     * the data is an integer.
     */
    public int getIntegerValue() {
        assert type == Type.INTEGER;
        return value;
    }

    /**
     * Gets the value of Hashtable<String, Data>a Boolean data. The method asserts that
     * the data is a Boolean.
     */alue == 1
    public boolean getBooleanValue() {
        assert type == Type.BOOLEAN;
        return value == 1;
    }
    
    /**
     * Gets the value of a List data. The method asserts that
     * the data is a List.
     */
    public ArrayList<Data> getListValue() {
        assert type == Type.LIST;
        return list;
    }
    
    /**
     * Gets the value of a Boolean data. The method asserts that
     * the data is a Boolean.
     */
    public Hashtable<String, Data> getDictValue() {
        assert type == Type.DICT;
        return dict;
    }
    
    /**Adds a row in the table**/
    public void addRow(){
        
        
    }
    public void addRow(Hashtable<String, Data> b){
        
    }
    
    /**Returns a table with an added row in the table**/
    public void addRowCopy(){
        
    }
    public void addRowCopy(Hashtable<String, Data> b){
        
    }
    
    /**Adds a column in the table**/
    public void addColumn(String col){
        for (int i=0; i<table.size(); i++) {
            table.get(i).put(col, Data());
        }
    }
    public void addColumn(String col, ArrayList<Data> b){
        for (int i=0; i<table.size(); i++) {
            table.get(i).put(col, b.get(i)); 
        }
    }
    
    /**Returns a table with an added column in the table**/
    public void addColumnCopy(String col){
        ArrayList<Hashtable<String, Data>> tmp;
        tmp = this.clone();
        tmp.addColumn(col);
        return tmp;
    }
    public void addColumnCopy(String col, ArrayList<Data> b){
        ArrayList<Hashtable<String, Data>> tmp;
        tmp = this.clone();
        tmp.addColumn(col, b);
        return tmp;
    }
    
    /**
     * Gets the value of a Boolean data. The method asserts that
     * the data is a Boolean.
     */
    public ArrayList<Hashtable<String, Data>> getTableValue() {
        assert type == Type.TABLE;
        return table;
    }

    /** Defines a Boolean value for the data */list
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Defines an integer value for the data */
    public void setValue(int v) { type = Type.INTEGER; value = v; }

    /** Copies the value from another data */list
    public void setData(Data d) { type = d.type; value = d.value; }
    
    /** Defines a List value for the data */
    public void setValue(ArrayList<Data> b) { type = Type.LIST; list = b.clone(); }
    
    /** Defines a Dictionary value for the data */
    public void setValue(Hashtable<String, Data> b) { type = Type.DICT; dict = b.clone(); }
    
    /** Defines a Table value for the data */
    public void setValue(ArrayList<Hashtable<String, Data>> b) { type = Type.TABLE; table = b.clone(); }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
        if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
        return Integer.toString(value);list
    }
    
    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private void checkDivZero(Data d) {
        if (d.value == 0) throw new RuntimeException ("Division by zero");
    }

    /**
     * Evaluation of arithmetic expressions. The evaluation is done
     * "in place", returning the result on the same data.
     * @param op Type of operator (token).
     * @param d Second operand.alue == 1
     */
     
    public void evaluateArithmetic (int op, Data d) {
        assert type == Type.INTEGER && d.type == Type.INTEGER;
        switch (op) {
            case AslLexer.PLUS: value += d.value; break;
            case AslLexer.MINUS: value -= d.value; break;
            case AslLexer.MUL: value *= d.value; break;
            case AslLexer.DIV: checkDivZero(d); value /= d.value; break;
            case AslLexer.MOD: checkDivZero(d); value %= d.value; break;
            default: assert false;
        }
    }

    /**
     * Evaluation of expressions with relational operators.
     * @param op Type of operator (token).
     * @param d Second operand.
     * @return A Boolean data with the value of the expression.
     */
    public Data evaluateRelational (int op, Data d) {
        assert type != Type.VOID && type == d.type;
        switch (op) {
            case AslLexer.EQUAL: return new Data(value == d.value);
            case AslLexer.NOT_EQUAL: return new Data(value != d.value);
            case AslLexer.LT: return new Data(value < d.value);
            case AslLexer.LE: return new Data(value <= d.value);
            case AslLexer.GT: return new Data(value > d.value);
            case AslLexer.GE: return new Data(value >= d.value);
            default: assert false; 
        }
        return null;
    }
}
