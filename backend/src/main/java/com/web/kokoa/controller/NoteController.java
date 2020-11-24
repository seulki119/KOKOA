package com.web.kokoa.controller;


import com.web.kokoa.model.speechnote;
import com.web.kokoa.model.subtitles;
import com.web.kokoa.model.user;
import com.web.kokoa.model.writenote;
import com.web.kokoa.repository.*;
import com.web.kokoa.service.SearchService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Failure"),
})

@CrossOrigin(origins = {"*"})
@Controller
@RequestMapping("/note")
public class NoteController {

    @Autowired
    SubtitlesRepo subtitlesRepo;

    @Autowired
    VideoRepo videoRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SpeechNoteRepo speechNoteRepo;

    @Autowired
    WriteNoteRepo writeNoteRepo;

    @Autowired
    SearchService searchService;

    @PostMapping("/insert")
    @ApiOperation(value = "오답노트 추가", notes = "user num에 맞는 video, subtitle 오답노트 추가.")
    public ResponseEntity<String> addtoNote(@RequestParam String email, @RequestParam int videoid, @RequestParam int subtitleid, @RequestParam int type, @RequestParam int engsubtitleid){

        int userid = userRepo.findUserByEmail(email).getId();
        String result = "";

            if (type == 0) {
                speechnote note = speechNoteRepo.getBySubtitleidandUserid(subtitleid,userid);

                if(note!=null){
                    result = "fail";
                    return new ResponseEntity<String>(result,HttpStatus.OK);
                }
                else {
                    note = new speechnote();
                    note.setUserid(userid);
                    note.setSubtitleid(subtitleid);
                    note.setEngsubtitleid(engsubtitleid);
                    note.setVideoid(videoid);
                    speechNoteRepo.save(note);
                    result = "success";
                }
            } else {
                writenote note = writeNoteRepo.getBySubtitleidandUserid(subtitleid, userid);
                if (note != null) {
                    result = "fail";
                    return new ResponseEntity<String>(result, HttpStatus.OK);
                } else {
                    note = new writenote();
                    note.setUserid(userid);
                    note.setSubtitleid(subtitleid);
                    note.setEngsubtitleid(engsubtitleid);
                    note.setVideoid(videoid);
                    writeNoteRepo.save(note);
                    result = "success";
                }
            }
            return new ResponseEntity<String>(result, HttpStatus.OK);
        }


    @GetMapping("/load")
    @ApiOperation(value = "오답노트 조회", notes = "user num에 맞는 video, subtitle 오답노트 조회.")
    public ResponseEntity<HashMap<String, List<Object>>> loadUserNote(@RequestParam String email, @RequestParam int page){
        user user = userRepo.findUserByEmail(email);
        int userid = user.getId();
        Page<writenote> writenotes = searchService.getWriteNote(page,userid);
        Page<speechnote> speechnotes = searchService.getSpeechNote(page,userid);

        HashMap<String,List<Object>> result = new HashMap<>();
        List<subtitles> write = new ArrayList<>();
        List<subtitles> speech = new ArrayList<>();
        for (writenote w: writenotes) {
            write.add(subtitlesRepo.getAllById(w.getSubtitleid()));
            write.add(subtitlesRepo.getAllById(w.getEngsubtitleid()));
        }
        for (speechnote s: speechnotes) {
            speech.add(subtitlesRepo.getAllById(s.getSubtitleid()));
            speech.add(subtitlesRepo.getAllById(s.getEngsubtitleid()));
        }
        result.put("writenote", Collections.singletonList(writenotes));
        result.put("writenote_sub", Collections.singletonList(write));
        result.put("speechnote", Collections.singletonList(speechnotes));
        result.put("speechnote_sub", Collections.singletonList(speech));

        return new ResponseEntity<HashMap<String, List<Object>>>(result,HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNote(@RequestParam String email, @RequestParam int noteid, @RequestParam int type) {
        user user = userRepo.findUserByEmail(email);
        int userid = user.getId();
        String result ="";
        try {
            if (type == 0) {
                speechnote deleteitem = speechNoteRepo.findById(noteid);
                speechNoteRepo.delete(deleteitem);
            } else {
                writenote writenote = writeNoteRepo.findById(noteid);
                writeNoteRepo.delete(writenote);
            }
            result="success";
        }catch (Exception e){
            result = "fail";
        }
        return new ResponseEntity<String>(result,HttpStatus.OK);
    }
}
