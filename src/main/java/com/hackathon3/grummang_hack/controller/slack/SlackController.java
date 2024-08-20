package com.hackathon3.grummang_hack.controller.slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/slack")
public class SlackController {



    @PostMapping("/init/channel")
    public ResponseEntity<?> SlackinitChannle(){

        return null;
    }

    @PostMapping("/init/users")
    public ResponseEntity<?> SlackinitUsers(){

            return null;
    }

    @PostMapping("/init/files")
    public ResponseEntity<?> SlackinitFiles(){

            return null;
    }

    @PostMapping("/init/all")
    public ResponseEntity<?> SlackinitAll(){

            return null;
    }
    @PostMapping("/board/files/recent")
    public ResponseEntity<?> SlackBoardFilesRecent(){

            return null;
    }
    @PostMapping("/board/files/size")
    public ResponseEntity<?> SlackBoardFilesSize(){

            return null;
    }
    @PostMapping("/board/files/count")
    public ResponseEntity<?> SlackBoardFilesCount(){

            return null;
    }

    @PostMapping("/board/user-ranking")
    public ResponseEntity<?> SlackBoardUserRanking(){

            return null;
    }
}
