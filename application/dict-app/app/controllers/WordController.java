package controllers;

import logics.WordLogic;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Int;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller{

    WordLogic logic = WordLogic.factory(null);

    public Result index(){
        return ok("বাংলা অভিধান এ স্বাগতম!" );
    }

    public Result testLength(String word, int length){

        int s_size = word.length();

        int len = Int.unbox(length);

        if(s_size == len)
            return ok("Yay! You made the correct character count of the word: " + word );
        if(s_size > len)
            return ok("Ups, the length of " + word + " is bigger than you thought!" );
        else
            return ok("Ups, the length of " + word + " is smaller than you thought!" );
    }

    public Result searchWordsBySpelling(String spelling){

        int limit = Integer.MAX_VALUE;

        return ok( logic.searchWordsBySpelling( spelling, limit).toString() );
    }

    public Result getWordBySpelling(String spelling){

        return ok( logic.getDictionaryWordBySpelling(spelling).toString() );
    }

    public Result getWordByWordId(String wordId){

        return ok( logic.getDictionaryWordByWordId( wordId ).toString() );
    }

}
