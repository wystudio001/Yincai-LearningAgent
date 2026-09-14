package xyz.wystudio.yincai.ai.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.wystudio.yincai.ai.application.service.ModelProviderApplicationService;
import xyz.wystudio.yincai.core.R;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/model-providers")
@RequiredArgsConstructor
public class AiModelProviderController {

    private final ModelProviderApplicationService modelProviderApplicationService;

    @GetMapping
    public R<List<Map<String, Object>>> listProviders() {
        return R.ok(modelProviderApplicationService.listProviders());
    }

    @PostMapping
    public R<Map<String, Object>> createProvider(@RequestBody Map<String, Object> request) {
        return R.ok(modelProviderApplicationService.createProvider(request));
    }

    @PostMapping("/models")
    public R<Map<String, Object>> fetchModels(@RequestBody Map<String, Object> request) {
        return R.ok(modelProviderApplicationService.fetchModels(request));
    }

    @PutMapping("/{id}")
    public R<Map<String, Object>> updateProvider(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return R.ok(modelProviderApplicationService.updateProvider(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteProvider(@PathVariable Long id) {
        modelProviderApplicationService.deleteProvider(id);
        return R.ok();
    }

    @PostMapping("/{id}/test")
    public R<Map<String, Object>> testProvider(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return R.ok(modelProviderApplicationService.testProvider(id, request));
    }
}
