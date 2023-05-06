package ru.linkstuff.friday.Splitter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

public class Splitter {

    private String SEARCH_TAG = "SEARCH";
    private String APP_TAG = "APP";

    private ArrayList<Query> query = new ArrayList<>();
    private String[] cachedRequest;

    private Dictionary dictionary;
    private TemporaryDictionary temp;

    private String[] extraChars = {
            "-", "(", ")", "%"
    };

    private String[] searchWords = {
            "как", "найди", "что", "когда", "зачем", "почему", "куда"
    };


    public static Splitter getSplitter(){
        return new Splitter();
    }

    public ArrayList<Query> getQuery(Context context, String request){
        dictionary = new Dictionary(context);

        request = removeExtraChars(request);
        cachedRequest = request.split(" и ");

        for (String r: cachedRequest){
            if (isSearch(r)) query.add(new Query(SEARCH_TAG, r));
            else {
                query.add(analyze(r));
            }
        }

        dictionary.closeDatabase();
        return query;
    }

    private Query analyze(String request){
        ArrayList<String> cache = new ArrayList<>(Arrays.asList(request.split(" ")));
        boolean goToNextStage = false;
        Query out = null;

        for (int stage = 0; stage < 4; ++stage){
            switch (stage){

                case 0: //Поиск команды
                    for (int position = 0; position < cache.size(); ++position){
                        if ((temp = dictionary.findCommand(cache.get(position))) != null){

                            if (temp.getId() == 3) out = new Query(APP_TAG, null);
                            else out = new Query(temp.getCommand(), null);

                            cache.remove(position);

                            break;
                        }

                    }
                    break;

                case 1: //Поиск дополнительных аргументов
                    if (temp.getId() == 1 || temp.getId() == 2){
                        for (int position = 0; position < cache.size(); ++ position){
                            if (dictionary.findExtraArg(cache.get(position)) != null){
                                out.setExtraArg(cache.get(position));
                                cache.remove(position);
                                break;
                            }
                        }
                    }
                    break;

                case 2: //Поиск аргументов изменения
                    if (temp.getId() == 2){
                        for (int position = 0; position < cache.size(); ++position){
                            if (dictionary.findArgOfChange(cache.get(position)) != null){
                                out.setArgOfChange(dictionary.findArgOfChange(cache.get(position)));
                                cache.remove(position);
                                break;
                            }
                        }

                    }
                    break;

                case 3: //Поиск аргументов
                    if (temp.getId() != 0){

                        for (int position = 0; position < cache.size(); ++position){

                            switch (temp.getId()){

                                case 1:
                                    if (isDigital(cache.get(position))){
                                        out.setArg(cache.get(position));
                                    }
                                    break;

                                case 2:
                                    if (isDigital(cache.get(position))){
                                        out.setArg(cache.get(position));
                                    }
                                    break;

                                case 3:
                                    if (cache.get(position).length() > 1) out.setArg(dictionary.findApp(cache.get(position)));
                                    break;

                                case 4:
                                    if (cache.get(position).length() > 1) out.setArg(dictionary.findDevice(cache.get(position)));
                                    break;

                            }

                            if (out.getArg() != null) break;

                        }


                    }

                    break;

            }

        }


        return out;
    }

    private String removeExtraChars(String request){
        for (String ch: extraChars) request = request.replace(ch, "");

        String[] cache = request.split(" ");
        request = "";

        for (int i = 0; i < cache.length; ++i){
            if (!isDigital(cache[i])) request += cache[i] + " ";
            else {
                String temp = "";
                while (i != cache.length && isDigital(cache[i])){
                    temp += cache[i];
                    ++i;
                }
                --i;

                request += temp + " ";
            }
        }

        return request;
    }

    private boolean isDigital(String string){

        try {
            Long.parseLong(string);
        } catch (NumberFormatException n){
            return false;
        }

        return true;
    }

    private boolean isSearch(String request){
        boolean isCommand = false;

        for (String search: searchWords){
            if (request.contains(search)) return true;
        }

        if (dictionary.findCommand(request) != null) isCommand = true;

        if (!isCommand) return true;

        return false;
    }

}
