package controllers;

import logics.WordLogic;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Int;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller{

    public Result index(){
        return ok("বাংলা অভিধান এ স্বাগতম!" );
    }

    public Result length(String word, int length){

        int s_size = word.length();

        int len = Int.unbox(length);

        if(s_size == len)
            return ok("Yay! You made the correct character count of the word: " + word );
        if(s_size > len)
            return ok("Ups, the length of " + word + " is bigger than you thought!" );
        else
            return ok("Ups, the length of " + word + " is smaller than you thought!" );
    }

    public Result getDictWord(String word){

        WordLogic logic = new WordLogic();

        return ok( logic.getDictWord( word ).getWordId() );

    }
}
