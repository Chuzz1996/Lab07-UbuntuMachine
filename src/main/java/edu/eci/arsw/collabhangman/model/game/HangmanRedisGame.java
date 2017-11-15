/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabhangman.model.game;

import edu.eci.arsw.collabhangman.services.GameServicesException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/**
 *
 * @author 2105409
 */
public class HangmanRedisGame extends HangmanGame {

    public int identificadorPartida;
    public StringRedisTemplate template;
    
    RedisScript<String> script;

    public HangmanRedisGame(String word, StringRedisTemplate srt, Integer id) {
        super(word);
        template = srt;
        identificadorPartida = id;
        script = script();
    }

    /**
     * @throws edu.eci.arsw.collabhangman.services.GameServicesException
     * @pre gameFinished==false
     * @param l new letter
     * @return the secret word with all the characters 'l' revealed
     */
    @Override
    public String addLetter(char l) throws GameServicesException {
        String letra = String.valueOf(l);
        String value = (String) template.opsForHash().get("game:" + identificadorPartida, "word");
        if (value!=null) {
            object[] myNameChars = new object[1];
            myNameChars[0] = letra;
            template.execute(new SessionCallback< List< Object>>() {
                @SuppressWarnings("unchecked")
                @Override
                public < K, V> List<Object> execute(final RedisOperations< K, V> operations) throws DataAccessException {
                    operations.watch((K) ("game:" + identificadorPartida + " currentWord"));
                    operations.multi();
                    operations.execute(script, Collections.singletonList((K)("game:"+identificadorPartida)), args);
                    return operations.exec();
                 }
            });
        } else {
            throw new GameServicesException("No existe dicha partida!!");
        }

        return value;
    }

    @Override
    public synchronized boolean tryWord(String playerName, String s) throws GameServicesException {
        String value = (String) template.opsForHash().get("game:" + identificadorPartida, "word");
        if (value!=null) {
            if (s.toLowerCase().equals(value)) {
                template.execute(new SessionCallback< List< Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public < K, V> List<Object> execute(final RedisOperations< K, V> operations) throws DataAccessException {
                        operations.watch((K) ("game:" + identificadorPartida + " currentWord"));
                        operations.multi();
                        operations.opsForHash().put((K)("game:" + identificadorPartida), "currentWord", value);
                        operations.opsForHash().put((K)("game:" + identificadorPartida), "winner", playerName);
                        operations.opsForHash().put((K)("game:" + identificadorPartida), "state", "true");
                        return operations.exec();
                    }
                });
                return true;
        } else {
            throw new GameServicesException("No existe dicha partida!!");
        }
        return false;
    }

    @Override
    public boolean gameFinished() throws GameServicesException {
        String value2 = (String) template.opsForHash().get("game:" + identificadorPartida, "state");
        if(value2==null){
            throw new GameServicesException("No existe dicha partida!!");
        }
        return !value2.equals("false");
    }

    /**
     * @throws edu.eci.arsw.collabhangman.services.GameServicesException
     * @pre gameFinished=true;
     * @return winner's name
     */
    @Override
    public String getWinnerName() throws GameServicesException {
        String value2 = (String) template.opsForHash().get("game:" + identificadorPartida, "winner");
        if(value2==null){
            throw new GameServicesException("No existe dicha partida!!");
        }
        return value2;
    }

    @Override
    public String getCurrentGuessedWord() throws GameServicesException {
        String value2 = (String) template.opsForHash().get("game:" + identificadorPartida, "currentWord");
        if(value2==null){
            throw new GameServicesException("No existe dicha partida!!");
        }
        return value2;
    }
    
    public RedisScript<String> script() {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/redis/scripts/luaFile.lua")));
        redisScript.setResultType(String.class);
        return redisScript;
    }

}