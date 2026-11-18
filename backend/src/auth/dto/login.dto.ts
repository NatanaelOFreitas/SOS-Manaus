import { Transform } from 'class-transformer';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export class LoginDto {
  @Transform(({ value }) => (typeof value === 'string' ? value.trim().toLowerCase() : value))
  @IsEmail({}, { message: 'Informe um e-mail válido.' })
  email!: string;

  @IsString()
  @IsNotEmpty({ message: 'Informe a senha.' })
  @MinLength(6, { message: 'A senha deve ter pelo menos 6 caracteres.' })
  senha!: string;
}
