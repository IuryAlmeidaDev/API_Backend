package br.com.projetoApi.Entity.Bloco.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Bloco.Dto.BlocoDTO;
import br.com.projetoApi.Entity.Bloco.Dto.StatusDTO;
import br.com.projetoApi.Entity.Bloco.Model.Bloco;
import br.com.projetoApi.Entity.Bloco.Model.Bloco.StatusBloco;
import br.com.projetoApi.Entity.Bloco.Repository.BlocoRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlocoService {

    @Autowired
    private BlocoRepository blocoRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public BlocoDTO criarBloco(BlocoDTO blocoDTO) {
        // Verifica se já existe um bloco com o mesmo nome
        if (blocoRepository.existsByNomeIgnoreCase(blocoDTO.getNome())) {
            throw new BlocoAlreadyExistsException("Já existe um bloco com o nome: " + blocoDTO.getNome());
        }

        Bloco bloco = new Bloco();
        bloco.setNome(blocoDTO.getNome());
        bloco.setDescricao(blocoDTO.getDescricao());
        bloco.setStatus(blocoDTO.getStatus() != null ? blocoDTO.getStatus() : StatusBloco.INATIVO);
        
        bloco = blocoRepository.save(bloco);
        auditService.register("BLOCO_CREATE", "Bloco criado: " + bloco.getNome());
        return convertToDTO(bloco);
    }

    @Transactional(readOnly = true)
    public List<BlocoDTO> listarBlocos() {
        return blocoRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlocoDTO obterBloco(Long id) {
        Bloco bloco = blocoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado com ID: " + id));
        return convertToDTO(bloco);
    }

    @Transactional
    public BlocoDTO editarBloco(Long id, BlocoDTO blocoDTO) {
        Bloco bloco = blocoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado com ID: " + id));
        
        // Verifica se o novo nome já existe em outro bloco
        if (!bloco.getNome().equalsIgnoreCase(blocoDTO.getNome()) && 
            blocoRepository.existsByNomeIgnoreCaseAndIdNot(blocoDTO.getNome(), id)) {
            throw new BlocoAlreadyExistsException("Já existe outro bloco com o nome: " + blocoDTO.getNome());
        }

        bloco.setNome(blocoDTO.getNome());
        bloco.setDescricao(blocoDTO.getDescricao());
        if (blocoDTO.getStatus() != null) {
            bloco.setStatus(blocoDTO.getStatus());
        }
        
        bloco = blocoRepository.save(bloco);
        auditService.register("BLOCO_UPDATE", "Bloco atualizado: " + bloco.getNome());
        return convertToDTO(bloco);
    }

    @Transactional
    public void deletarBloco(Long id) {
        if (!blocoRepository.existsById(id)) {
            throw new EntityNotFoundException("Bloco não encontrado com ID: " + id);
        }
        blocoRepository.deleteById(id);
        auditService.register("BLOCO_DELETE", "Bloco removido: " + id);
    }

    @Transactional
    public BlocoDTO alterarStatusBloco(Long id, StatusDTO statusDTO) {
        Bloco bloco = blocoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado com ID: " + id));
        
        bloco.setStatus(statusDTO.getStatus());
        bloco = blocoRepository.save(bloco);
        auditService.register("BLOCO_STATUS_UPDATE", "Status alterado do bloco " + bloco.getId() + " para " + bloco.getStatus());
        return convertToDTO(bloco);
    }

    @Transactional(readOnly = true)
    public List<BlocoDTO> listarBlocosPorStatus(StatusBloco status) {
        return blocoRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BlocoDTO> buscarBlocosPorNome(String nome) {
        return blocoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BlocoDTO> listarBlocosAtivos() {
        return blocoRepository.findBlocosAtivos()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BlocoDTO convertToDTO(Bloco bloco) {
        BlocoDTO dto = new BlocoDTO();
        dto.setId(bloco.getId());
        dto.setNome(bloco.getNome());
        dto.setDescricao(bloco.getDescricao());
        dto.setStatus(bloco.getStatus());
        dto.setCreatedAt(bloco.getCreatedAt());
        dto.setUpdatedAt(bloco.getUpdatedAt());
        return dto;
    }

    // Exceções personalizadas
    public static class BlocoAlreadyExistsException extends RuntimeException {
        public BlocoAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class BlocoNotFoundException extends RuntimeException {
        public BlocoNotFoundException(String message) {
            super(message);
        }
    }
}
