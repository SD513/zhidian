package com.bupt.zhidian.controller;

import com.bupt.zhidian.entity.Note;
import com.bupt.zhidian.entity.Picture;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.Binary;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Api(tags = "决策笔记")
@RestController
@RequestMapping(value = "/note")
public class NoteController {
    @Autowired
    private MongoTemplate mongoTemplate;

    @ApiOperation("上传笔记")
    @PostMapping(value = "uploadNote")
    public String uploadNote(@RequestBody Note note) {
        if(note == null) {
            JSONObject jsonObject = new JSONObject().put("status", -1).put("msg", "笔记为空");
            return jsonObject.toString();
        }
        mongoTemplate.insert(note, "notes");
        JSONObject jsonObject = new JSONObject().put("status", 0).put("msg", "上传笔记成功");
        return jsonObject.toString();
    }

    @ApiOperation("获取用户笔记")
    @PostMapping(value = "getNoteById")
    public String getNoteById(@RequestParam(value = "userId") String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Note> notes = mongoTemplate.find(query, Note.class, "notes");
        if(notes.isEmpty()) {
            JSONObject jsonObject = new JSONObject().put("status", -2).put("msg", "结果为空");
            return jsonObject.toString();
        } else {
            JSONObject jsonObject = new JSONObject().put("status", 0).put("data", notes);
            return jsonObject.toString();
        }
    }

    @ApiOperation("更改用户笔记")
    @PostMapping(value = "updateNote")
    public String updateNoteById(@RequestBody Note note) {
        mongoTemplate.save(note, "notes");
        JSONObject jsonObject = new JSONObject().put("status", 0).put("msg", "更改成功");
        return jsonObject.toString();
    }

    @ApiOperation("删除用户笔记")
    @DeleteMapping(value = "updateNoteById")
    public String deleteNoteById(@RequestParam(value = "noteId") String noteId) {
        Query query = new Query(Criteria.where("_id").is(noteId));
        mongoTemplate.remove(query, Note.class, noteId);
        JSONObject jsonObject = new JSONObject().put("status", 0).put("msg", "删除成功");
        return jsonObject.toString();
    }

    @ApiOperation("上传图片")
    @PostMapping(value = "uploadPicture")
    public String uploadPicture(@RequestParam(value = "image") MultipartFile file) {
        if(file.isEmpty()) {
            JSONObject jsonObject = new JSONObject().put("status", -1).put("msg", "图片为空");
            return jsonObject.toString();
        }

        try {
            String fileName = file.getOriginalFilename();
            Picture picture = new Picture()
                    .setName(fileName)
                    .setCreatedTime(LocalDateTime.now())
                    .setSize(file.getSize())
                    .setContent(new Binary(file.getBytes()))
                    .setContentType(file.getContentType());
            mongoTemplate.insert(picture, "pictures");
            String url = "http://localhost:5055/note/image/" + picture.getId();
            JSONObject jsonObject = new JSONObject().put("status", 0).put("url", url);
            return jsonObject.toString();
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject jsonObject = new JSONObject().put("status", -2).put("msg", "图片上传失败");
            return jsonObject.toString();
        }

    }

    @ApiOperation("获取图片url")
    @GetMapping(value = "image/{id}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public byte[] getPicture(@PathVariable("id") String id) {
        byte[] data = null;
        Picture file = mongoTemplate.findById(id, Picture.class, "pictures");
        if (file != null) {
            data = file.getContent().getData();
        }
        return data;
    }
}
