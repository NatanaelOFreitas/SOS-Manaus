import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

/** FRONTEIRA: barra requisições sem token válido no cabeçalho Authorization. */
@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {}
