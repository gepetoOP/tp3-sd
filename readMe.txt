MAQUINA COM O TSE:

	ela que vai distribuir as outras entre Subject, Observer e Writer, aleatoriamente.
		numero de Subject é fixo (define na classe TSE)
		numero de Writer é metade das maquinas que sobrarem (total - n de subjects)
		numero de Observers são as que sobrarem (essas são as que abrem a janela c os pontinhos)
	
	Ela não "se distribui" pra nenhum dos 3 (pq a invoker tem que ta rodando -> confirmar se da pra rodar a Invoker E a TSE depois)
	
	As funções Invoke basicamente mandam uma mensagem pro IP
		conteudo [<numero>, argumentos]
		Subject: numero 1, argumento: <ips dos outros subjects>, <todos os subjects e suas portas>, <o ip que ta sendo invocado>, BATCH_SIZE?, nuvem_inicial
		Writer: numero 2, argumento: <ips dos subjects>
		Observer: numero 3, argumento: <porta do ip que ta sendo invocado>
	
	Eu AAACHO que a funcao Server fica vigiando se algum Subject caiu, pra avisar os outros subjects disso, mas n sei 
		
		
	Execução:
		Main()
			pega os ip de td mundo
			pega as portas dos observers (??????????????????????? ENTENDER DIREITO ISSO)
			TSE()
				"sorteia" o que que cada máquina vai ser
			invokeObservers()
				manda msg pras máquinas que vao ser observer (avisando que elas vao ser observers rs)
			Sleep(1000)
			invokeSubjects()
				idem
			Sleep(1000)
			invokeWriters()
				idem
			Sleep(1000)
			Server()
			    se receber mensagem que um subject morreu, notifica os observers
			    tenta reconectar no subject
			    se conseguir, notifica os observers que voltou



MAQUINAS INVOKER:

	Classe invoker só serve pra abrir a conexão com o TSE e receber a mensagem falando se vai ser Subject, Observer ou Writer
	(e chama o construtor certo tmb, no caso)


	main():
	    chama o construtor

	invoker()
	    fica escutando na porta 6789 (Configs.INVOKER_PORTA)
	    recebe mensagem do TSE -> msgHandler()

	msgHnadler()
	    olha se vai ser Subject, Observer ou Writer
	    chama o respectivo contrutor



SUBJECT:
    subject(ips_dos_outros_subjects, todos_os_subjects_ip_e_porta, proprio_ip)

    nuvemInicial()
        coloca um tanto de ponto no dotBatch

        notifyObserversUDP()
            manda mensagem pros observers que escutam a mesma porta que ele??
            pra td mundo??

    server()
        recebe mensagem

        msgHandler()
            1 - recebe pontinhos de writer

                sync()
                    manda mensagem pros outros subjects

            2 - recebe sincronizacao de outro subjects



OBSERVER:
    observer(propria_porta)

    cria o frame

    server()
        recebe packets

        msgHandler()
            printa os pontos ou trata a queda de um subject

            printDots()
                só atualiza o panel()


WRITER:
    writer(ips_dos_subjects)

    generateDots()
        generateDot()
            write()
                sorteia um subject pra escrever (nao manda pro mesmo 2x seguidas)

                se o subject falha, notifica o TSE (repetidamente)