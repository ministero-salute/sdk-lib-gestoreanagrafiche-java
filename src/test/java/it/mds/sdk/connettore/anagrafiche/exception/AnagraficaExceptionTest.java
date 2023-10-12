package it.mds.sdk.connettore.anagrafiche.exception;

import it.mds.sdk.connettore.anagrafiche.exception.AnagraficaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnagraficaExceptionTest {

    @Test
    void AnagraficaException(){
            AnagraficaException e = new AnagraficaException("message");
            assertTrue(e instanceof AnagraficaException);
    }
    @Test
    void AnagraficaException2(){
        Throwable t = Mockito.mock(Throwable.class);
        AnagraficaException e = new AnagraficaException("", t);
        assertTrue(e instanceof AnagraficaException);
    }
}
