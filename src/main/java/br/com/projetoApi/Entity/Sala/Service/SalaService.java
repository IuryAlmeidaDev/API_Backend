package br.com.projetoApi.Entity.Sala.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Bloco.Model.Bloco;
import br.com.projetoApi.Entity.Bloco.Repository.BlocoRepository;
import br.com.projetoApi.Entity.Sala.Dto.SalaDTO;
import br.com.projetoApi.Entity.Sala.Dto.SalaStatusDTO;
import br.com.projetoApi.Entity.Sala.Model.Sala;
import br.com.projetoApi.Entity.Sala.Model.Sala.StatusSala;
import br.com.projetoApi.Entity.Sala.Model.Sala.TipoSala;
import br.com.projetoApi.Entity.Sala.Repository.SalaRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private BlocoRepository blocoRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public SalaDTO criarSala(SalaDTO salaDTO) {
        // Verifica se já existe uma sala com o mesmo nome
        if (salaRepository.existsByNomeIgnoreCase(salaDTO.getNome())) {
            throw new SalaAlreadyExistsException("Já existe uma sala com o nome: " + salaDTO.getNome());
        }

        // Verifica se o bloco existe
        Bloco bloco = blocoRepository.findById(salaDTO.getBlocoId())
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado com ID: " + salaDTO.getBlocoId()));

        Sala sala = new Sala();
        sala.setNome(salaDTO.getNome());
        sala.setCapacidade(salaDTO.getCapacidade());
        sala.setStatus(salaDTO.getStatus() != null ? salaDTO.getStatus() : StatusSala.LIVRE);
        sala.setTipoSala(salaDTO.getTipoSala());
        sala.setBloco(bloco);
        
        sala = salaRepository.save(sala);
        auditService.register("SALA_CREATE", "Sala criada: " + sala.getNome());
        return convertToDTO(sala);
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalas() {
        return salaRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalaDTO obterSala(Long id) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + id));
        return convertToDTO(sala);
    }

    @Transactional
    public SalaDTO editarSala(Long id, SalaDTO salaDTO) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + id));
        
        // Verifica se o novo nome já existe em outra sala
        if (!sala.getNome().equalsIgnoreCase(salaDTO.getNome()) && 
            salaRepository.existsByNomeIgnoreCaseAndIdNot(salaDTO.getNome(), id)) {
            throw new SalaAlreadyExistsException("Já existe outra sala com o nome: " + salaDTO.getNome());
        }

        // Verifica se o bloco existe
        Bloco bloco = blocoRepository.findById(salaDTO.getBlocoId())
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado com ID: " + salaDTO.getBlocoId()));

        sala.setNome(salaDTO.getNome());
        sala.setCapacidade(salaDTO.getCapacidade());
        sala.setTipoSala(salaDTO.getTipoSala());
        sala.setBloco(bloco);
        
        if (salaDTO.getStatus() != null) {
            sala.setStatus(salaDTO.getStatus());
        }
        
        sala = salaRepository.save(sala);
        auditService.register("SALA_UPDATE", "Sala atualizada: " + sala.getNome());
        return convertToDTO(sala);
    }

    @Transactional
    public void deletarSala(Long id) {
        if (!salaRepository.existsById(id)) {
            throw new EntityNotFoundException("Sala não encontrada com ID: " + id);
        }
        salaRepository.deleteById(id);
        auditService.register("SALA_DELETE", "Sala removida: " + id);
    }

    @Transactional
    public SalaDTO alterarStatusSala(Long id, SalaStatusDTO statusDTO) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala não encontrada com ID: " + id));
        
        sala.setStatus(statusDTO.getStatus());
        sala = salaRepository.save(sala);
        auditService.register("SALA_STATUS_UPDATE", "Status alterado da sala " + sala.getId() + " para " + sala.getStatus());
        return convertToDTO(sala);
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasPorStatus(StatusSala status) {
        return salaRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasPorTipo(TipoSala tipoSala) {
        return salaRepository.findByTipoSala(tipoSala)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasPorBloco(Long blocoId) {
        return salaRepository.findByBlocoIdOrderByNomeAsc(blocoId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasLivres() {
        return salaRepository.findSalasLivres()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasPorCapacidadeMinima(Integer capacidade) {
        return salaRepository.findByCapacidadeGreaterThanEqual(capacidade)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> listarSalasLivresComCapacidadeMinima(Integer capacidade) {
        return salaRepository.findSalasLivresComCapacidadeMinima(capacidade)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> buscarSalasComFiltros(Long blocoId, StatusSala status, TipoSala tipoSala, Integer capacidadeMinima) {
        return salaRepository.findSalasComFiltros(blocoId, status, tipoSala, capacidadeMinima)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long contarSalasLivresPorBloco(Long blocoId) {
        return salaRepository.countSalasLivresPorBloco(blocoId);
    }

    @Transactional(readOnly = true)
    public List<SalaDTO> buscarSalasPorNome(String nome) {
        return salaRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SalaDTO convertToDTO(Sala sala) {
        SalaDTO dto = new SalaDTO();
        dto.setId(sala.getId());
        dto.setNome(sala.getNome());
        dto.setCapacidade(sala.getCapacidade());
        dto.setStatus(sala.getStatus());
        dto.setTipoSala(sala.getTipoSala());
        dto.setBlocoId(sala.getBloco().getId());
        dto.setBlocoNome(sala.getBloco().getNome());
        dto.setCreatedAt(sala.getCreatedAt());
        dto.setUpdatedAt(sala.getUpdatedAt());
        return dto;
    }

    // Exceções personalizadas
    public static class SalaAlreadyExistsException extends RuntimeException {
        public SalaAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class SalaNotFoundException extends RuntimeException {
        public SalaNotFoundException(String message) {
            super(message);
        }
    }
}
