import { Module } from '@nestjs/common';
import { SaudeController } from './fronteira/saude.controller';

@Module({ controllers: [SaudeController] })
export class SaudeModule {}
