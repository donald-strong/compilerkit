package au.com.illyrian.dsl.pc;

import au.com.illyrian.parser.Lexer;
import au.com.illyrian.parser.ParseClass;
import au.com.illyrian.parser.ParserException;
import au.com.illyrian.parser.impl.Latin1Lexer;
import au.com.illyrian.parser.impl.ParserBase;

/**
 * Compiler generator for Domain Specific Languages (DLS)
 *  
 * @author dstrong
 */
public class PC extends ParserBase implements ParseClass
{
    public PC()
    {
        setLexer(createLexer());
    }
    
    protected Latin1Lexer createLexer()
    {
        return new PCLexer();
    }
   
    /**
     *  class_body  ::= '{' abilities char_attrs '}'
     *  attribute   ::= char_attr 
     *  char_attr   ::= name_attr race_attr class_attr sex_attr align_attr abilities many_attrs
     *  name_attr   ::= "Name" ':' IDENTIFIER
     *  race_attr   ::= "Race" ':' "human" | "elf" | "dwarf" | "halfling"
     *  class_attr  ::= "Class" ':' ("fighter" | "magic-user" | "cleric" | "anti-cleric" | "thief")
     *  | error("Expected Class : one of ...")
     *  sex_attr    ::= "Sex" ':' "male" | "female"
     *  align_attr  ::= "Alignment" ':' "lawful" | "neutral" | "chaotic"
     *  abilities   ::= "Abilities" ':' '{' many_abilities '}'
     *  many_abilities ::= str_attr int_attr wis_attr dex_attr con_attr cha_attr
     *  str_attr    ::= "Strength" ':' ability_score
     *  int_attr    ::= "Intelligence" ':' ability_score
     *  wis_attr    ::= "Wisdom" ':' ability_score
     *  dex_attr    ::= "Dexterity" ':' ability_score
     *  con_attr    ::= "Constitution" ':' ability_score
     *  cha_attr    ::= "Charisma" ':' ability_score
     *  ability_score ::= int {3..18}
     */
    public Object parseClass() throws ParserException
    {
        nextToken();
        
        class_body();
        return null;
    }
    
    //*  class_body  ::= '{' abilities char_attrs '}'
    public void class_body() throws ParserException
    {
        expect(Lexer.OPEN_P, "{", "'{' expected.");
        abilities();
        char_attrs();
        //other_attrs();
        expect(Lexer.CLOSE_P, "}", "'}' expected.");
    }

    //*  char_attr   ::= name_attr race_attr class_attr sex_attr align_attr
    public void char_attrs() throws ParserException
    {
        name_attr();
        race_attr();
        class_attr();
        sex_attr();
        align_attr();
    }
    //*  name_attr   ::= "Name" ':' SPAN_EOLN
    public void name_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Name");
        expect(Lexer.OPERATOR, ":");
        String name = getLexer().spanToEndOfLine();
        nextToken();
    }
    
    boolean listContains(String candidate, String[] optionsList)
    {
        for (int i=0; i<optionsList.length; i++)
            if (optionsList[i].equals(candidate.toLowerCase().trim()))
                return true;
        return false;
    }
    
    String toString(String [] optionsList)
    {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<optionsList.length; i++)
        {
            if (buf.length() > 0)
                buf.append(", ");
            buf.append(optionsList[i]);
        }
        return buf.toString();
    }
    
    //*  race_attr   ::= "Race" ':' "human" | "elf" | "dwarf" | "halfling"
    public static final String [] optionsRace = {"human", "elf", "dwarf", "halfling"};
    public void race_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Race");
        expect(Lexer.OPERATOR, ":");
        String value = expect(Lexer.IDENTIFIER, null);
        if (!listContains(value, optionsRace))
            throw error(getInput(), "Expected: " + toString(optionsRace));
    }
    //*  class_attr  ::= "Class" ':' "fighter" | "magicuser" | "cleric" | "anticleric" | "thief"
    public static final String [] optionsClass = {"fighter", "magicuser", "cleric", "anticleric", "thief"};
    public void class_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Class");
        expect(Lexer.OPERATOR, ":");
        String value = expect(Lexer.IDENTIFIER, null);
        if (!listContains(value, optionsClass))
            throw error(getInput(), "Expected one of: " + toString(optionsClass));
    }

    //*  sex_attr    ::= "Sex" ':' "male" | "female"
    public static final String [] optionsSex = {"male", "female"};
    public void sex_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Sex");
        expect(Lexer.OPERATOR, ":");
        String value = expect(Lexer.IDENTIFIER, null);
        if (!listContains(value, optionsSex))
            throw error(getInput(), "Expected one of: " + toString(optionsSex));
    }
    
    //*  align_attr  ::= "Alignment" ':' "lawful" | "neutral" | "chaotic"
    public static final String [] optionsAlign = {"lawful", "neutral", "chaotic"};
    public void align_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Alignment");
        expect(Lexer.OPERATOR, ":");
        String value = expect(Lexer.IDENTIFIER, null);
        if (!listContains(value, optionsAlign))
            throw error(getInput(), "Expected one of: " + toString(optionsAlign));
    }
    
    //*  abilities   ::= "Abilities" ':' '{' many_abilities '}'
    public void abilities() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Abilities");
        expect(Lexer.OPERATOR, ":");
        expect(Lexer.OPEN_P, "{");
        many_abilities();
        expect(Lexer.CLOSE_P, "}");
    }
    
    //*  many_abilities ::= str_attr int_attr wis_attr dex_attr con_attr cha_attr
    public void many_abilities() throws ParserException
    {
        str_attr();
        int_attr();
        wis_attr();
        dex_attr();
        con_attr();
        cha_attr();
    }
    
    //*  str_attr    ::= "Strength" ':' ability_score
    public void str_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Str");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Str");
    }
    
    //*  int_attr    ::= "Intelligence" ':' ability_score
    public void int_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Int");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Int");
    }
    
    //*  wis_attr    ::= "Wisdom" ':' ability_score
    public void wis_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Wis");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Wis");
    }
    
    //*  dex_attr    ::= "Dexterity" ':' ability_score
    public void dex_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Dex");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Dex");
    }
    
    //*  con_attr    ::= "Constitution" ':' ability_score
    public void con_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Con");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Con");
    }
    
    //*  cha_attr    ::= "Charisma" ':' ability_score
    public void cha_attr() throws ParserException
    {
        expect(Lexer.IDENTIFIER, "Cha");
        expect(Lexer.OPERATOR, ":");
        int score = ability_score("Cha");
    }
    
    int ability_score(String name) throws ParserException
    {
        int score = 0;
        if (match(Lexer.INTEGER, null)) {
            score = getLexer().getTokenInteger();
            if (3 <= score && score <= 18)
            {
                nextToken();
                return score;
            }
        }
        throw error(getInput(), name + " must be a number in the range 3..18");
    }
    
}
