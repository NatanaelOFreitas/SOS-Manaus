import { PrismaClient, Perfil } from '@prisma/client';
import * as bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  const senhaHash = await bcrypt.hash('sos12345', 10);

  await prisma.usuario.upsert({
    where: { email: 'cidadao@sosmanaus.com' },
    update: {},
    create: {
      nome: 'Maria Silva',
      email: 'cidadao@sosmanaus.com',
      senhaHash,
      perfil: Perfil.CIDADAO,
    },
  });

  await prisma.usuario.upsert({
    where: { email: 'gestor@sosmanaus.com' },
    update: {},
    create: {
      nome: 'Gestor Prefeitura',
      email: 'gestor@sosmanaus.com',
      senhaHash,
      perfil: Perfil.GESTOR,
    },
  });

  console.log('Seed concluído: cidadao@sosmanaus.com / gestor@sosmanaus.com (senha: sos12345)');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
