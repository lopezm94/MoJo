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
import java.util.HashMap;
import java.util.Map;

public abstract class Data {

  public abstract Data deepClone();

  public abstract String getType();

  public abstract BooleanData evaluateRelational(int op, Data data);

  public static boolean isType (String type, Data b) {
      return b.getType() == type;
  }

  public Data evaluateArithmetic(int op, Data data){
    throw new RuntimeException("Data type not supported for arithmetic operations");
  }

  // Transoform object to Data equivalent
  public static Data toData(Object o) {
    if (o == null) return new VoidData();
    throw new RuntimeException("No Data type not supported for "+o.getClass());
  }
  public static Data toData(Boolean data) {
      return new BooleanData(data);
  }
  public static Data toData(Integer data) {
      return new IntegerData(data);
  }
  public static Data toData(String data) {
      return new StringData(data);
  }
  public static Data toData(ArrayList data) {
    ListData<Data> res = new ListData<Data>();
    for (int i=0; i<data.size(); i++) {
      res.add(Data.toData(data.get(i)));
    }
    return res;
  }
  public static Data toData(HashMap<String, Object> data) {
    DictData res = new DictData();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      res.put(entry.getKey(), Data.toData(entry.getValue()));
    }
    return res;
  }

  public static Data parse(String elem) {
    elem = elem.trim();
    if (elem.length() == 0)
      return new VoidData();
    if (elem.equals("true"))
      return new BooleanData(true);
    if (elem.equals("false"))
      return new BooleanData(false);
    if (elem != null && elem.matches("[-+]?\\d*\\.?\\d+"))
      return new IntegerData(Integer.parseInt(elem));
    if (elem.charAt(elem.length()-1) == '\'' && elem.charAt(0) == '\'')
      return new StringData(elem.substring(1,elem.length()-1));
    throw new RuntimeException("Can't parse " + elem);
  }

}
