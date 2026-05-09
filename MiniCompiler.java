import java.util.*; 
 
// Main class 
public class MiniCompiler { 
 
   // ===== TOKEN CLASS ===== 
   static class Token { 
       String type; 
       String value; 
 
       Token(String type, String value) { 
           this.type = type; 
           this.value = value; 
       } 
 
       public String toString() { 
           return value; 
       } 
   } 
 
   // ===== TOKENIZER ===== 
   static class Tokenizer { 
       String input; 
       int pos = 0; 
 
       Tokenizer(String input) { 
           this.input = input.replaceAll("\\s+", ""); 
       } 
 
       List<Token> tokenize() { 
           List<Token> tokens = new ArrayList<>(); 
 
           while (pos < input.length()) { 
               char ch = input.charAt(pos); 
 
               // NUMBERS 
               if (Character.isDigit(ch)) { 
                   StringBuilder num = new StringBuilder(); 
 
                   while (pos < input.length() && Character.isDigit(input.charAt(pos))) { 
                       num.append(input.charAt(pos)); 
                       pos++; 
                   } 
 
                   // Check for ++ 
                   if (pos + 1 < input.length() 
                           && input.charAt(pos) == '+' 
                           && input.charAt(pos + 1) == '+') { 
 
                       // Convert n++ into (n + 1) 
                       tokens.add(new Token("NUMBER", num.toString())); 
                       tokens.add(new Token("OP", "+")); 
                       tokens.add(new Token("NUMBER", "1")); 
 
                       pos += 2; 
                   } else { 
                       tokens.add(new Token("NUMBER", num.toString())); 
                   } 
               } 
 
               // OPERATORS 
               else if ("+-*/".indexOf(ch) != -1) { 
                   tokens.add(new Token("OP", String.valueOf(ch))); 
                   pos++; 
               } 
 
               // LEFT PAREN 
               else if (ch == '(') { 
                   tokens.add(new Token("LPAREN", "(")); 
                   pos++; 
               } 
 
               // RIGHT PAREN 
               else if (ch == ')') { 
                   tokens.add(new Token("RPAREN", ")")); 
                   pos++; 
               } 
 
               else { 
                   throw new RuntimeException("Invalid character: " + ch); 
               } 
           } 
 
           return tokens; 
       } 
   } 
 
   // ----- TREE NODE ----- 
   static class Node { 
       String value; 
       Node left, right; 
 
       Node(String value) { 
           this.value = value; 
       } 
   } 
 
   // ===== PARSER ===== 
   static class Parser { 
       List<Token> tokens; 
       int pos = 0; 
 
       Parser(List<Token> tokens) { 
           this.tokens = tokens; 
       } 
 
       Node parse() { 
           Node result = expression(); 
 
           if (pos != tokens.size()) { 
               throw new RuntimeException("Unexpected token at end"); 
           } 
 
           return result; 
       } 
 
       // E → T ((+|-) T)* 
       Node expression() { 
           Node node = term(); 
 
           while (pos < tokens.size() 
                   && (tokens.get(pos).value.equals("+") 
                   || tokens.get(pos).value.equals("-"))) { 
 
               String op = tokens.get(pos).value; 
               pos++; 
 
               Node right = term(); 
 
               Node newNode = new Node(op); 
               newNode.left = node; 
               newNode.right = right; 
 
               node = newNode; 
           } 
 
           return node; 
       } 
 
       // T → F ((*|/) F)* 
       Node term() { 
           Node node = factor(); 
 
           while (pos < tokens.size() 
                   && (tokens.get(pos).value.equals("*") 
                   || tokens.get(pos).value.equals("/"))) { 
 
               String op = tokens.get(pos).value; 
               pos++; 
 
               Node right = factor(); 
 
               Node newNode = new Node(op); 
               newNode.left = node; 
               newNode.right = right; 
 
               node = newNode; 
           } 
 
           return node; 
       } 
 
       // F → (E) | number 
       Node factor() { 
 
           if (pos >= tokens.size()) { 
               throw new RuntimeException("Unexpected end of input"); 
           } 
 
           Token token = tokens.get(pos); 
 
           // Parentheses 
           if (token.value.equals("(")) { 
               pos++; 
 
               Node node = expression(); 
 
               if (pos >= tokens.size() || !tokens.get(pos).value.equals(")")) { 
                   throw new RuntimeException("Missing closing parenthesis"); 
               } 
 
               pos++; 
               return node; 
           } 
 
           // Number 
           if (token.type.equals("NUMBER")) { 
               pos++; 
               return new Node(token.value); 
           } 
 
           // Unary minus 
           if (token.value.equals("-")) { 
               pos++; 
 
               Node node = factor(); 
 
               Node newNode = new Node("-"); 
               newNode.left = new Node("0"); 
               newNode.right = node; 
 
               return newNode; 
           } 
 
           throw new RuntimeException("Unexpected token: " + token.value); 
       } 
   } 
 
   // ===== EVALUATOR ===== 
   static class Evaluator { 
 
       static int evaluate(Node node) { 
 
           if (node.left == null && node.right == null) { 
               return Integer.parseInt(node.value); 
           } 
 
           int left = evaluate(node.left); 
           int right = evaluate(node.right); 
 
           switch (node.value) { 
               case "+": 
                   return left + right; 
 
               case "-": 
                   return left - right; 
 
               case "*": 
                   return left * right; 
 
               case "/": 
                   return left / right; 
           } 
 
           throw new RuntimeException("Invalid operator"); 
       } 
   } 
 
   // ===== TREE PRINTER ===== 
   static void printTree(Node node, String indent, boolean last) { 
 
       if (node == null) 
           return; 
 
       System.out.print(indent); 
 
       if (last) { 
           System.out.print(" └── "); 
           indent += "    "; 
       } else { 
           System.out.print(" ├── "); 
           indent += "│   "; 
       } 
 
       System.out.println(node.value); 
 
       printTree(node.left, indent, false); 
       printTree(node.right, indent, true); 
   } 
 
   // ===== MAIN ===== 
   public static void main(String[] args) { 
 
       Scanner scanner = new Scanner(System.in); 
 
       while (true) { 
 
           System.out.print("Enter expression: "); 
           String input = scanner.nextLine(); 
 
           if (input.equalsIgnoreCase("exit")) { 
               break; 
           } 
 
           try { 
 
               // Tokenize 
               Tokenizer tokenizer = new Tokenizer(input); 
               List<Token> tokens = tokenizer.tokenize(); 
 
               System.out.println("Tokens: " + tokens); 
 
               // Parse 
               Parser parser = new Parser(tokens); 
               Node tree = parser.parse(); 
 
               System.out.println("Parse: Success"); 
 
               // Print Tree 
               System.out.println("Expression Tree:"); 
               printTree(tree, "", true); 
 
               // Evaluate 
               int result = Evaluator.evaluate(tree); 
               System.out.println("Result: " + result); 
           } 
 
           catch (Exception e) { 
               System.out.println("Error: " + e.getMessage()); 
           } 
 
           System.out.println(); 
       } 
 
       scanner.close(); 
   } 
} 
