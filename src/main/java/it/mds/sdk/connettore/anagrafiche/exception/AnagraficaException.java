/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.exception;

public class AnagraficaException extends RuntimeException{

    public AnagraficaException(String message) {
        super(message);
    }

    public AnagraficaException(String message, Throwable cause) {
        super(message, cause);
    }

}
