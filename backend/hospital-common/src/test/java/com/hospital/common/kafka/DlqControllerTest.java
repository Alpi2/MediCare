package com.hospital.common.kafka;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DlqControllerTest {

    @Test
    void listReturnsPage() {
        DlqService svc = Mockito.mock(DlqService.class);
        Pageable p = PageRequest.of(0, 10);
        Mockito.when(svc.listDlqMessages(p)).thenReturn(new PageImpl<>(List.of()));
        DlqController c = new DlqController(svc);
        ResponseEntity<?> r = c.list(p);
        assertEquals(HttpStatus.OK, r.getStatusCode());
    }

    @Test
    void getNotFound() {
        DlqService svc = Mockito.mock(DlqService.class);
        Mockito.when(svc.getMessage(1L)).thenReturn(null);
        DlqController c = new DlqController(svc);
        ResponseEntity<?> r = c.get(1L);
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    void replayIncrements() {
        DlqService svc = Mockito.mock(DlqService.class);
        DlqMessage m = new DlqMessage(); m.setId(1L);
        Mockito.when(svc.getMessage(1L)).thenReturn(m);
        DlqController c = new DlqController(svc);
        ResponseEntity<?> r = c.replay(1L);
        assertEquals(HttpStatus.ACCEPTED, r.getStatusCode());
        Mockito.verify(svc).incrementReplayAttempt(1L, null);
    }

    @Test
    void resolveMarksProcessed() {
        DlqService svc = Mockito.mock(DlqService.class);
        DlqMessage m = new DlqMessage(); m.setId(1L);
        Mockito.when(svc.getMessage(1L)).thenReturn(m);
        DlqController c = new DlqController(svc);
        ResponseEntity<?> r = c.resolve(1L);
        assertEquals(HttpStatus.NO_CONTENT, r.getStatusCode());
        Mockito.verify(svc).markProcessed(1L);
    }
}
