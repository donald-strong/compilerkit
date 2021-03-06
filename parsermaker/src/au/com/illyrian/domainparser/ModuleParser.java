package au.com.illyrian.domainparser;


import au.com.illyrian.parser.Input;
import au.com.illyrian.parser.Lexer;
import au.com.illyrian.parser.ParseModule;
import au.com.illyrian.parser.ParserException;
import au.com.illyrian.parser.impl.InvokeParser;
import au.com.illyrian.parser.impl.Latin1Lexer;
import au.com.illyrian.parser.impl.ParserBase;
import au.com.illyrian.parser.maker.ModuleActionMaker;

/**
*
* Loads a domain specific language parser and delegates to it.
* <p>
* The Domain Parser is a generic parser that can parse any domain specific language.
* It does this by loading a specific parser for the langauge and then delegating
* the parsing of the domain specific language to that parser.
*  
* The Domain Parser only does three things:
* <OL>
* <LI>it imports one or more classes</LI>
* <LI>it instantiates one of the imported classes that implements the GenericParser interface</LI>
* <LI>it delegates the parsing of the rest of the code to the instantiated parser.
* </OL>
* 
* <code>
* import au.com.illyrian.exprparser.Expr
* 
* Expr::{
* 
*   func1(a, b) = a + b;
*   
*   func2(a, b, c) = a * b + c;
* }::Expr
* </code>
* 
* @author Donald Strong
*/
public class ModuleParser extends ParserBase implements ParseModule
{
   /** The actions to be applied to the recognised input tokens. */
   private ModuleAction                 moduleAction = null;

   private String                       lastIdentifier;
   
   /**
    * Public constructor for the search query parser. When no actions are provided the parser only performs validation.
    */
   public ModuleParser()
   {
       setLexer(createLexer());
       getLexer().getReservedWords().setProperty("package", "package");
       getLexer().getReservedWords().setProperty("import", "import");
   }

    public ModuleAction getModuleAction()
    {
        if (moduleAction == null)
            moduleAction = createModuleAction();
        return moduleAction;
    }

    public void setModuleAction(ModuleAction actions)
    {
        this.moduleAction = actions;
    }
    
    protected ModuleAction createModuleAction()
    {
        ModuleAction action = new ModuleActionMaker();
        getCompileUnit().visitParser(action);
        return action;
    }

    protected Latin1Lexer createLexer()
    {
        return new Latin1Lexer();
    }
   
   /**
    * Parse the given Domain Specific Language.
    * <p>
    * This is a recursive descent parser. <br>
    * The parser rules are expressed in Extended Bacus Nour Form (EBNF). <br>
    * <code>
    *     dec_module   ::= dec_package dec_imports dec_class
    *     dec_package  ::= [ 'package' classname ';' ]
    *     dec_imports  ::= { 'import' classname ';' }
    *     classname    ::= IDENTIFIER { '.' IDENTIFIER } 
    *     dec_class    ::= IDENTIFIER '::' code '::' IDENTIFIER ';'
    *     code         ::= <code for the invoked parser> 
    * </code>
    *
    * @param input - shared input for the parser
    * @return the result of parsing the input and applying actions from ExpressionAction.
    * @throws ParserException - if an error occurs.
    */
   public Object parseModule() throws ParserException
   {
       // Read the first token from input.
       nextToken();

       // Parse top level expression.
       Object module = dec_module(); 
       // Ensure all tokens have been processed.
       endModule();

       return module;
   }
   
   protected void endModule() throws ParserException
   {
       int token = getToken();
       // Ensure all tokens have been processed.
       if (token == Lexer.ERROR)
       {
           throw error(getInput(), getLexer().getErrorMessage());
       }
       else if (token == Lexer.CLOSE_P)
       {
           throw error(getInput(), "Unbalanced perentheses - too many \')\'.");
       }
       else if (token != Lexer.END)
       {
           throw error(getInput(), "End of input expected");
       }
   }
   
   /** dec_module ::= dec_package more_imports dec_class */
   public Object dec_module() throws ParserException
   {
       Object decModule = packageStatement();
       decModule = more_imports();
       decModule = dec_class();
       return decModule;
   }
   
   /** packageStatement   ::= [ 'package' classname ';' ] */
   public Object packageStatement() throws ParserException
   {
       if (accept(Lexer.RESERVED, "package"))
       {
           String packageName = classname();
           expect(Lexer.DELIMITER, ";", "';' expected at the end of the package name");
           
           return getModuleAction().declarePackage(packageName);
       }
       return null;
   }

   /** more_imports ::= { 'import' classname ';' } */
   public Object more_imports() throws ParserException
   {
       if (accept(Lexer.RESERVED, "import"))
       {
           String className = classname();
           String simpleName = lastIdentifier;
           expect(Lexer.DELIMITER, ";", "';' expected at end of fully qualified class name");
           more_imports();

           getModuleAction().declareImport(className, simpleName);
           getModuleAction().setAlias(simpleName, className.toString());
       }
       return getModuleAction().getModule();
   }

   /** classname ::= IDENTIFIER { '.' IDENTIFIER } */
   public String classname() throws ParserException
   {
       String qualifiedClassName = null;
       if (getToken() == Lexer.IDENTIFIER)
       {
           String simpleName = getLexer().getTokenValue();
           lastIdentifier = simpleName;
           nextToken();
           qualifiedClassName = getModuleAction().addClassName(qualifiedClassName, simpleName);

           while (accept(Lexer.OPERATOR, "."))
           {
               if (getToken() == Lexer.IDENTIFIER)
               {
                   simpleName = getLexer().getTokenValue();
                   lastIdentifier = simpleName;
                   nextToken();
                   qualifiedClassName = getModuleAction().addClassName(qualifiedClassName, simpleName);
               }
               else
                   throw error(getInput(), "More package name expected.");
           }
       }
       else
           throw error(getInput(), "Class name expected.");
       return qualifiedClassName;
   }

   /** more_classname :== { '.' IDENTIFIER } */
//   public String more_classname() throws ParserException
//   {
//       String qualifiedClassName = null;
//       if (accept(Lexer.OPERATOR, "."))
//       {
//           if (getToken() == Lexer.IDENTIFIER)
//           {
//               String simpleName = getLexer().getTokenValue();
//               lastIdentifier = simpleName;
//               nextToken();
//               qualifiedClassName = more_classname();
//
//               qualifiedClassName = getModuleAction().addClassName(simpleName, qualifiedClassName);
//           }
//           else
//               throw error(getInput(), "More package name expected.");
//       }
//       return qualifiedClassName;
//   }

   /**
    *     classname ::= IDENTIFIER { '.' IDENTIFIER } ';'
    *     parser    ::= IDENTIFIER '::' code '::' IDENTIFIER ';'
    *
    * @return the result of ExpressionAction.actionIdentifier(IDENTIFIER) or
    *         ExpressionAction.actionPerentheses(and_expr).
    * @throws Exception -
    *             if an error occurs.
    */
   public Object dec_class() throws ParserException
   {
       if (getToken() == Lexer.IDENTIFIER)
       {
           Object parseName = classname();
           if (match(Lexer.OPERATOR, "::"))
           {
               String qualifiedName = getModuleAction().getAlias(parseName.toString());
               InvokeParser parser = getCompileUnit().getInvokeParser();
               Input input = getInput();
               Object result = parser.invokeParseClass(qualifiedName, input);
               getModuleAction().handleModule(result);

               nextToken();
               if (accept(Lexer.OPERATOR, "::")) 
               {
                   Object className = classname();
                   if (!className.equals(parseName))
                       throw error(getInput(), "'" + parseName + "' expected at end of parser space");
               }
               else
                   throw error(getInput(), ":: expected");
           }
           else
               throw error(getInput(), ":: expected");
       }
       else
           throw error(getInput(), "Parser name expected");
       return getModuleAction().getModule();
   }
}
