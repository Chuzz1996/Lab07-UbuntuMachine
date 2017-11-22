/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabhangman.restcontrollers;

import edu.eci.arsw.collabhangman.model.game.entities.User;
import edu.eci.arsw.collabhangman.services.GameServices;
import edu.eci.arsw.collabhangman.services.GameServicesException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hcadavid
 */
@RestController
@RequestMapping("/users")
public class UsersResourceController {

 
    
    @Autowired
    GameServices gameServices;
    
    @Autowired
    SimpMessagingTemplate msmt;
    
    @RequestMapping(method = RequestMethod.GET)
    public String test(){
        return "Ok";
    }

    @RequestMapping(path="/{userid}",method = RequestMethod.GET)
    public ResponseEntity<?> getUserById(@PathVariable int userid) {
        try {
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(gameServices.loadUserData(userid), HttpStatus.ACCEPTED);
        } catch (Exception  ex) {
            Logger.getLogger(UsersResourceController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @RequestMapping(path = "/scores", method = RequestMethod.GET)
    public ResponseEntity<?> getScores() {
        return new ResponseEntity<>(gameServices.loadScores(), HttpStatus.ACCEPTED);
    }

}
