package com.ai.demo_ai_service.service;

import com.ai.demo_ai_service.config.AiConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {


  @Autowired
  private AiConfig aiConfig;


  private final OkHttpClient client =
      new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS).build();

  // 🔥 固定格式指令（自动美化排版）
  private static final String FORMAT_PROMPT = 
  "請用繁體中文、清晰排版回答：\n" + 
  "- 重点内容**加粗**\n"+ 
  "- 分段清晰\n" + 
  "- 使用列表，例如：\n1. 項目一\n2. 項目二\n3. 項目三\n" + 
  "- 适合网页阅读\n" + 
  "问题：";

  // 普通聊天
  public String chat(String question) {
    JSONObject body = new JSONObject();
    body.put("model", "deepseek-chat");

    // 🔥 自动加入美化指令
    String finalQuestion = FORMAT_PROMPT + question; 
    JSONArray messages = JSONArray.of(JSONObject.of("role", "user", "content", finalQuestion));
    body.put("messages", messages);

    try (Response response = client.newCall(getRequest(body)).execute()) {
      if (!response.isSuccessful() || response.body() == null)
        return "请求失败：" + response.code();

      String resStr = response.body().string();
      JSONObject res = JSON.parseObject(resStr);
      return res.getJSONArray("choices").getJSONObject(0)
          .getJSONObject("message").getString("content");
    } catch (Exception e) {
      return "异常：" + e.getMessage();
    }
  }

  // 流式输出
  public Flux<String> streamChat(String question) {
    JSONObject body = new JSONObject();
    body.put("model", "deepseek-chat");
    body.put("stream", true);

    // 🔥 自动加入美化指令（关键！）
    String finalQuestion = FORMAT_PROMPT + question;
    
    body.put("messages",JSONArray.of(JSONObject.of("role", "user", "content", finalQuestion)));

    return Flux.create(emitter -> {
      client.newCall(getRequest(body)).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          emitter.next("请求失败");
          emitter.complete();
        }

        @Override
        public void onResponse(Call call, Response response) {
          try (ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null) {
              emitter.next("请求失败：" + response.code());
              emitter.complete();
              return;
            }

            String line;
            while ((line = body.source().readUtf8Line()) != null) {
              if (line.isBlank() || !line.startsWith("data:"))
                continue;
              String data = line.substring(5).trim();
              if ("[DONE]".equals(data))
                break;

              try {
                JSONObject res = JSON.parseObject(data);
                String content = res.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("delta").getString("content");
                if (content != null)
                  emitter.next(content);
              } catch (Exception ignored) {
              }
            }
            emitter.complete();
          } catch (Exception e) {
            emitter.next("请求失败");
            emitter.complete();
          }
        }
      });
    });
  }

  private Request getRequest(JSONObject body) {
    return new Request.Builder().url(aiConfig.getApiUrl())
        .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
        .addHeader("Content-Type", "application/json").post(RequestBody
            .create(body.toString(), MediaType.parse("application/json")))
        .build();
  }

  // 内容审核
  public String audit(String content) {
    return chat("你是内容审核员：" + content);
  }

  // 数据分析
  public String analyzeData(String data, String question) {
    return chat("数据：" + data + " 问题：" + question);
  }

  // 知识库
  public String knowledgeQA(String knowledge, String question) {
    return chat("知识库：" + knowledge + " 问题：" + question);
  }
}
