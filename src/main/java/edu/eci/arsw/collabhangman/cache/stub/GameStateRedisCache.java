/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabhangman.cache.stub;

import edu.eci.arsw.collabhangman.cache.GameStateCache;
import edu.eci.arsw.collabhangman.model.game.HangmanRedisGame;
import edu.eci.arsw.collabhangman.services.GameCreationException;
import edu.eci.arsw.collabhangman.services.GameServicesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author 2105409
 */


@Service
public class GameStateRedisCache implements GameStateCache {
    
        
    public GameStateRedisCache(){
    
    }
    
    @Autowired
    private StringRedisTemplate template;  
    
    
    
    @Override
    public void createGame(int id, String word) throws GameCreationException {
        String value=(String)template.opsForHash().get("game:"+id, word); 
        if (!value.equals("nil")){
            throw new GameCreationException("The game "+id+" already exist.");
        }
        else{
            System.out.println("CREO NUEVO HAGMAN "+id+"  "+word);
            template.opsForHash().put("game:"+id,"word", word);
            String current="";
            for(int i=0;i<word.length();i++){current+="_";}
            template.opsForHash().put("game:"+id,"currentWord", current);
            template.opsForHash().put("game:"+id, "state","false");
            template.opsForHash().put("game:"+id, "winner","");
        }
    }

    /**
     *
     * @param gameid
     * @return
     * @throws GameServicesException
     */
    @Override
    public HangmanRedisGame getGame(int gameid) throws GameServicesException {
        return new HangmanRedisGame("", template, gameid);
    }

    public StringRedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void cleanCache(Integer gameid) {
        String value=(String)template.opsForHash().get("game:"+gameid, "word"); 
        String current="";
        for(int i=0;i<value.length();i++){current+="_";}
        template.opsForHash().put("game:"+gameid,"currentWord",current);
        template.opsForHash().put("game:"+gameid,"state","false");
        template.opsForHash().put("game:"+gameid,"winner","");
    }
    
    
}
