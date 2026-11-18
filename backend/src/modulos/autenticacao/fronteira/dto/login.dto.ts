import { Transform } from 'class-transformer';
import { IsEmail, IsEnum, IsNotEmpty, IsString, MinLength } from 'class-validator';
import { Canal } from '../../entidade/perfil';

/** Formato da requisição POST /auth/login (validado na fronteira, antes do controle). */
export class LoginDto {
  @Transform(({ value }) => (typeof value === 'string' ? value.trim().toLowerCase() : value))
  @IsEmail({}, { message: 'Informe um e-mail válido.' })
  email!: string;

  @IsString()
  @IsNotEmpty({ message: 'Informe a senha.' })
  @MinLength(6, { message: 'A senha deve ter pelo menos 6 caracteres.' })
  senha!: string;

  @IsEnum(Canal, { message: 'Canal inválido. Use APP_CIDADAO ou PAINEL_PREFEITURA.' })
  canal!: Canal;
}
