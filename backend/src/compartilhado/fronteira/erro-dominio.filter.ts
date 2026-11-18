import { ArgumentsHost, Catch, ExceptionFilter, HttpStatus } from '@nestjs/common';
import { Response } from 'express';
import { ErroDominio } from '../entidade/erro-dominio';

/**
 * FRONTEIRA: traduz erros de domínio para respostas HTTP.
 * É o único lugar que sabe qual status HTTP corresponde a cada regra violada.
 */
@Catch(ErroDominio)
export class ErroDominioFilter implements ExceptionFilter {
  private static readonly STATUS_POR_CODIGO: Record<string, number> = {
    CREDENCIAIS_INVALIDAS: HttpStatus.UNAUTHORIZED,
    SESSAO_INVALIDA: HttpStatus.UNAUTHORIZED,
    USUARIO_INATIVO: HttpStatus.FORBIDDEN,
    CANAL_NAO_PERMITIDO: HttpStatus.FORBIDDEN,
  };

  catch(erro: ErroDominio, host: ArgumentsHost): void {
    const resposta = host.switchToHttp().getResponse<Response>();
    const status = ErroDominioFilter.STATUS_POR_CODIGO[erro.codigo] ?? HttpStatus.UNPROCESSABLE_ENTITY;

    resposta.status(status).json({
      statusCode: status,
      codigo: erro.codigo,
      message: erro.message,
    });
  }
}
