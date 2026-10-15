package br.edu.ifms.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.ifms.emprestimo.entity.Emprestimo;
import br.edu.ifms.emprestimo.repository.EmprestimoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmprestimoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmprestimoRepository repository;

    @BeforeEach
    void limparBancoDeTeste() {
        repository.deleteAll();
    }

    @Test
    void listarSemCadastrosRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/emprestimos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cadastrarRetorna201ComIdGeradoEPersisteTodosOsCampos() throws Exception {
        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"livroId": 10, "leitorId": 20, "dataEmprestimo": "2026-10-15"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.livroId").value(10))
                .andExpect(jsonPath("$.leitorId").value(20))
                .andExpect(jsonPath("$.dataEmprestimo").value("2026-10-15"));

        assertThat(repository.findAll()).singleElement().satisfies(emprestimo -> {
            assertThat(emprestimo.getId()).isPositive();
            assertThat(emprestimo.getLivroId()).isEqualTo(10L);
            assertThat(emprestimo.getLeitorId()).isEqualTo(20L);
            assertThat(emprestimo.getDataEmprestimo()).isEqualTo(LocalDate.of(2026, 10, 15));
        });
    }

    @Test
    void listarRetornaTodosOsEmprestimosCadastrados() throws Exception {
        salvarEmprestimo(10L, 20L, LocalDate.of(2026, 10, 15));
        salvarEmprestimo(30L, 40L, LocalDate.of(2026, 10, 16));

        mockMvc.perform(get("/emprestimos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].livroId", containsInAnyOrder(10, 30)))
                .andExpect(jsonPath("$[*].leitorId", containsInAnyOrder(20, 40)))
                .andExpect(jsonPath("$[*].dataEmprestimo", containsInAnyOrder("2026-10-15", "2026-10-16")));
    }

    @Test
    void cadastrarComIdEnviadoNaoSobrescreveEmprestimoExistente() throws Exception {
        Emprestimo existente = salvarEmprestimo(10L, 20L, LocalDate.of(2026, 10, 15));

        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": %d, "livroId": 30, "leitorId": 40, "dataEmprestimo": "2026-10-16"}
                                """.formatted(existente.getId())))
                .andExpect(status().isCreated());

        assertThat(repository.count()).isEqualTo(2);
        Emprestimo preservado = repository.findById(existente.getId()).orElseThrow();
        assertThat(preservado.getLivroId()).isEqualTo(10L);
        assertThat(preservado.getLeitorId()).isEqualTo(20L);
        assertThat(preservado.getDataEmprestimo()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(repository.findAll()).extracting(Emprestimo::getLivroId).containsExactlyInAnyOrder(10L, 30L);
    }

    private Emprestimo salvarEmprestimo(Long livroId, Long leitorId, LocalDate dataEmprestimo) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivroId(livroId);
        emprestimo.setLeitorId(leitorId);
        emprestimo.setDataEmprestimo(dataEmprestimo);
        return repository.save(emprestimo);
    }
}
