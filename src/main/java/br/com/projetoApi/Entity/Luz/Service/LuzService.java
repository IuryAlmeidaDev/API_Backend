package br.com.projetoApi.Entity.Luz.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Luz.Dto.LuzDTO;
import br.com.projetoApi.Entity.Luz.Dto.LuzStatusDTO;
import br.com.projetoApi.Entity.Luz.Model.Luz;
import br.com.projetoApi.Entity.Luz.Model.Luz.StatusLuz;
import br.com.projetoApi.Entity.Luz.Repository.LuzRepository;
import br.com.projetoApi.Entity.Sala.Model.Sala;
import br.com.projetoApi.Entity.Sala.Repository.SalaRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LuzService {

    @Autowired
    private LuzRepository luzRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public LuzDTO criarLuz(LuzDTO luzDTO) {
        // Verifica se a sala existe
        Sala sala = salaRepository.findById(luzDTO.getSalaId())
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + luzDTO.getSalaId()));

        // Verifica se já existe uma luz na sala
        if (luzRepository.existsBySalaId(luzDTO.getSalaId())) {
            throw new LuzAlreadyExistsException("Já existe uma luz cadastrada para a sala: " + sala.getNome());
        }

        Luz luz = new Luz();
        luz.setSala(sala);
        luz.setStatus(luzDTO.getStatus() != null ? luzDTO.getStatus() : StatusLuz.DESLIGADO);
        
        luz = luzRepository.save(luz);
        auditService.register("LUZ_CREATE", "Luz criada para sala " + sala.getNome());
        return convertToDTO(luz);
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzes() {
        return luzRepository.findAllOrderBySalaNome()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LuzDTO obterLuz(Long id) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        return convertToDTO(luz);
    }

    @Transactional
    public LuzDTO editarLuz(Long id, LuzDTO luzDTO) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        
        // Verifica se a sala existe (se estiver sendo alterada)
        if (!luz.getSala().getId().equals(luzDTO.getSalaId())) {
            Sala novaSala = salaRepository.findById(luzDTO.getSalaId())
                    .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + luzDTO.getSalaId()));
            
            // Verifica se já existe luz na nova sala
            if (luzRepository.existsBySalaId(luzDTO.getSalaId())) {
                throw new LuzAlreadyExistsException("Já existe uma luz cadastrada para a sala: " + novaSala.getNome());
            }
            
            luz.setSala(novaSala);
        }
        
        if (luzDTO.getStatus() != null) {
            luz.setStatus(luzDTO.getStatus());
        }
        
        luz = luzRepository.save(luz);
        auditService.register("LUZ_UPDATE", "Luz atualizada: " + luz.getId());
        return convertToDTO(luz);
    }

    @Transactional
    public void deletarLuz(Long id) {
        if (!luzRepository.existsById(id)) {
            throw new EntityNotFoundException("Luz não encontrada com ID: " + id);
        }
        luzRepository.deleteById(id);
        auditService.register("LUZ_DELETE", "Luz removida: " + id);
    }

    @Transactional
    public LuzDTO alterarStatusLuz(Long id, LuzStatusDTO statusDTO) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        
        luz.setStatus(statusDTO.getStatus());
        luz = luzRepository.save(luz);
        auditService.register("LUZ_STATUS_UPDATE", "Status alterado da luz " + luz.getId() + " para " + luz.getStatus());
        return convertToDTO(luz);
    }

    @Transactional
    public LuzDTO ligarLuz(Long id) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        
        luz.setStatus(StatusLuz.LIGADO);
        luz = luzRepository.save(luz);
        auditService.register("LUZ_TURN_ON", "Luz ligada: " + luz.getId());
        return convertToDTO(luz);
    }

    @Transactional
    public LuzDTO desligarLuz(Long id) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        
        luz.setStatus(StatusLuz.DESLIGADO);
        luz = luzRepository.save(luz);
        auditService.register("LUZ_TURN_OFF", "Luz desligada: " + luz.getId());
        return convertToDTO(luz);
    }

    @Transactional
    public LuzDTO alternarStatusLuz(Long id) {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Luz não encontrada com ID: " + id));
        
        StatusLuz novoStatus = luz.getStatus() == StatusLuz.LIGADO ? StatusLuz.DESLIGADO : StatusLuz.LIGADO;
        luz.setStatus(novoStatus);
        luz = luzRepository.save(luz);
        auditService.register("LUZ_TOGGLE", "Luz alternada: " + luz.getId() + " para " + novoStatus);
        return convertToDTO(luz);
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzesPorStatus(StatusLuz status) {
        return luzRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzesLigadas() {
        return luzRepository.findLuzesLigadas()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzesDesligadas() {
        return luzRepository.findLuzesDesligadas()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzesPorBloco(Long blocoId) {
        return luzRepository.findByBlocoId(blocoId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LuzDTO> listarLuzesPorSala(Long salaId) {
        return luzRepository.findBySalaId(salaId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long contarLuzesLigadasPorBloco(Long blocoId) {
        return luzRepository.countLuzesLigadasPorBloco(blocoId);
    }

    @Transactional(readOnly = true)
    public Long contarLuzesLigadasPorSala(Long salaId) {
        return luzRepository.countLuzesLigadasPorSala(salaId);
    }

    @Transactional
    public List<LuzDTO> ligarTodasLuzesDoBloco(Long blocoId) {
        List<Luz> luzes = luzRepository.findByBlocoId(blocoId);
        luzes.forEach(luz -> luz.setStatus(StatusLuz.LIGADO));
        luzRepository.saveAll(luzes);
        auditService.register("LUZ_BLOCO_TURN_ON", "Todas as luzes do bloco foram ligadas: " + blocoId);
        
        return luzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LuzDTO> desligarTodasLuzesDoBloco(Long blocoId) {
        List<Luz> luzes = luzRepository.findByBlocoId(blocoId);
        luzes.forEach(luz -> luz.setStatus(StatusLuz.DESLIGADO));
        luzRepository.saveAll(luzes);
        auditService.register("LUZ_BLOCO_TURN_OFF", "Todas as luzes do bloco foram desligadas: " + blocoId);
        
        return luzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LuzDTO> ligarTodasLuzesDaSala(Long salaId) {
        // Verifica se a sala existe
        salaRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + salaId));
        
        List<Luz> luzes = luzRepository.findBySalaId(salaId);
        luzes.forEach(luz -> luz.setStatus(StatusLuz.LIGADO));
        luzRepository.saveAll(luzes);
        auditService.register("LUZ_SALA_TURN_ON", "Todas as luzes da sala foram ligadas: " + salaId);
        
        return luzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LuzDTO> desligarTodasLuzesDaSala(Long salaId) {
        // Verifica se a sala existe
        salaRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + salaId));
        
        List<Luz> luzes = luzRepository.findBySalaId(salaId);
        luzes.forEach(luz -> luz.setStatus(StatusLuz.DESLIGADO));
        luzRepository.saveAll(luzes);
        auditService.register("LUZ_SALA_TURN_OFF", "Todas as luzes da sala foram desligadas: " + salaId);
        
        return luzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LuzDTO convertToDTO(Luz luz) {
        LuzDTO dto = new LuzDTO();
        dto.setId(luz.getId());
        dto.setSalaId(luz.getSala().getId());
        dto.setSalaNome(luz.getSala().getNome());
        dto.setBlocoNome(luz.getSala().getBloco().getNome());
        dto.setStatus(luz.getStatus());
        dto.setCreatedAt(luz.getCreatedAt());
        dto.setUpdatedAt(luz.getUpdatedAt());
        return dto;
    }

    // Exceções personalizadas
    public static class LuzAlreadyExistsException extends RuntimeException {
        public LuzAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class LuzNotFoundException extends RuntimeException {
        public LuzNotFoundException(String message) {
            super(message);
        }
    }

}
