package br.com.projetoApi.Entity.Unidade.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Unidade.Dto.UnidadeCreateRequest;
import br.com.projetoApi.Entity.Unidade.Dto.UnidadeResponse;
import br.com.projetoApi.Entity.Unidade.Dto.UnidadeUpdateRequest;
import br.com.projetoApi.Entity.Unidade.Model.Unidade;
import br.com.projetoApi.Entity.Unidade.Repository.UnidadeRepository;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;
    private final AuditService auditService;

    public UnidadeService(UnidadeRepository unidadeRepository, AuditService auditService) {
        this.unidadeRepository = unidadeRepository;
        this.auditService = auditService;
    }

    @Transactional
    public UnidadeResponse create(UnidadeCreateRequest request) {
        Unidade unidade = new Unidade();
        unidade.setNome(request.getNome().trim());
        unidade.setTipo(request.getTipo());
        unidade.setAtivo(request.getAtivo() == null || request.getAtivo());
        Unidade saved = unidadeRepository.save(unidade);
        auditService.register("UNIDADE_CREATE", "Unidade criada: " + saved.getNome());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listAll() {
        return unidadeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UnidadeResponse update(Long unidadeId, UnidadeUpdateRequest request) {
        Unidade unidade = getUnidade(unidadeId);
        unidade.setNome(request.getNome().trim());
        unidade.setTipo(request.getTipo());
        auditService.register("UNIDADE_UPDATE", "Unidade atualizada: " + unidade.getNome());
        return toResponse(unidade);
    }

    @Transactional
    public UnidadeResponse activate(Long unidadeId) {
        Unidade unidade = getUnidade(unidadeId);
        unidade.setAtivo(true);
        auditService.register("UNIDADE_ACTIVATE", "Unidade ativada: " + unidade.getNome());
        return toResponse(unidade);
    }

    @Transactional
    public UnidadeResponse deactivate(Long unidadeId) {
        Unidade unidade = getUnidade(unidadeId);
        unidade.setAtivo(false);
        auditService.register("UNIDADE_DEACTIVATE", "Unidade desativada: " + unidade.getNome());
        return toResponse(unidade);
    }

    private Unidade getUnidade(Long unidadeId) {
        return unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Unidade nao encontrada.", "Nao existe unidade para o identificador informado."));
    }

    public UnidadeResponse toResponse(Unidade unidade) {
        UnidadeResponse response = new UnidadeResponse();
        response.setId(unidade.getId());
        response.setNome(unidade.getNome());
        response.setTipo(unidade.getTipo());
        response.setAtivo(unidade.isAtivo());
        return response;
    }
}
