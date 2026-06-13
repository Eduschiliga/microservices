from datetime import datetime
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


ROOT = Path(__file__).resolve().parents[1]
REPORT_DIR = ROOT / "reports" / "rendered"
PDF_PATH = REPORT_DIR / "microservices-test-report.pdf"

BLUE = colors.HexColor("#2E74B5")
DARK_BLUE = colors.HexColor("#1F4D78")
MUTED = colors.HexColor("#595959")
LIGHT_GRAY = colors.HexColor("#F2F4F7")
CALLOUT = colors.HexColor("#F4F6F9")
BORDER = colors.HexColor("#C9D2DD")


def styles():
    base = getSampleStyleSheet()
    base.add(
        ParagraphStyle(
            name="ReportTitle",
            parent=base["Title"],
            fontName="Helvetica-Bold",
            fontSize=21,
            leading=25,
            alignment=TA_LEFT,
            spaceAfter=5,
            textColor=colors.black,
        )
    )
    base.add(
        ParagraphStyle(
            name="Subtitle",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=10.5,
            leading=13,
            spaceAfter=12,
            textColor=MUTED,
        )
    )
    base.add(
        ParagraphStyle(
            name="Body",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=10,
            leading=12.5,
            spaceAfter=7,
            textColor=colors.black,
        )
    )
    base.add(
        ParagraphStyle(
            name="Heading1Custom",
            parent=base["Heading1"],
            fontName="Helvetica-Bold",
            fontSize=15,
            leading=18,
            spaceBefore=14,
            spaceAfter=7,
            textColor=BLUE,
        )
    )
    base.add(
        ParagraphStyle(
            name="TableCell",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=8.6,
            leading=10.5,
            spaceAfter=0,
        )
    )
    base.add(
        ParagraphStyle(
            name="TableHeader",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=8.8,
            leading=10.5,
            spaceAfter=0,
            textColor=DARK_BLUE,
        )
    )
    base.add(
        ParagraphStyle(
            name="CalloutTitle",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=10.3,
            leading=12,
            spaceAfter=3,
            textColor=DARK_BLUE,
        )
    )
    return base


def p(text, style):
    return Paragraph(text.replace("\n", "<br/>"), style)


def key_value_table(rows, style_sheet):
    data = [[p(label, style_sheet["TableHeader"]), p(value, style_sheet["TableCell"])] for label, value in rows]
    table = Table(data, colWidths=[1.55 * inch, 4.95 * inch], hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("GRID", (0, 0), (-1, -1), 0.35, BORDER),
                ("BACKGROUND", (0, 0), (0, -1), LIGHT_GRAY),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 7),
                ("RIGHTPADDING", (0, 0), (-1, -1), 7),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def matrix(headers, rows, widths, style_sheet):
    data = [[p(h, style_sheet["TableHeader"]) for h in headers]]
    data.extend([[p(str(v), style_sheet["TableCell"]) for v in row] for row in rows])
    table = Table(data, colWidths=widths, hAlign="LEFT", repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("GRID", (0, 0), (-1, -1), 0.35, BORDER),
                ("BACKGROUND", (0, 0), (-1, 0), LIGHT_GRAY),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
            ]
        )
    )
    return table


def callout(title, body, style_sheet):
    data = [[p(title, style_sheet["CalloutTitle"]), p(body, style_sheet["TableCell"])]]
    table = Table(data, colWidths=[1.35 * inch, 5.15 * inch], hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("GRID", (0, 0), (-1, -1), 0.35, BORDER),
                ("BACKGROUND", (0, 0), (-1, -1), CALLOUT),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 8),
                ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                ("TOPPADDING", (0, 0), (-1, -1), 8),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
            ]
        )
    )
    return table


def header_footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(inch, letter[1] - 0.55 * inch, "Relatorio de validacao - Microservices")
    canvas.drawRightString(letter[0] - inch, 0.55 * inch, f"Pagina {doc.page}")
    canvas.restoreState()


def build():
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    style_sheet = styles()
    doc = SimpleDocTemplate(
        str(PDF_PATH),
        pagesize=letter,
        rightMargin=inch,
        leftMargin=inch,
        topMargin=0.88 * inch,
        bottomMargin=0.82 * inch,
        title="Relatorio de Validacao do Projeto Microservices",
        author="Codex",
    )

    story = [
        p("Relatorio de Validacao do Projeto Microservices", style_sheet["ReportTitle"]),
        p(
            "Execucao local, testes automatizados e validacao ponta a ponta via API Gateway, Eureka, Kafka e PostgreSQL.",
            style_sheet["Subtitle"],
        ),
        key_value_table(
            [
                ("Data da execucao", datetime.now().strftime("%d/%m/%Y %H:%M:%S")),
                ("Workspace", str(ROOT)),
                ("Resultado geral", "Aprovado: build, containers e fluxo funcional validados."),
            ],
            style_sheet,
        ),
        Spacer(1, 10),
        p("Resumo Executivo", style_sheet["Heading1Custom"]),
        callout(
            "Status final",
            "O ambiente Docker subiu com todos os servicos saudaveis. O fluxo de login, autenticacao no gateway, criacao de pedido, publicacao Kafka, processamento de pagamento e atualizacao do pedido para PAID foi validado com sucesso.",
            style_sheet,
        ),
        Spacer(1, 8),
        p(
            "A validacao entrou pelo endpoint publico do gateway em localhost:9090. O usuario admin autenticou no servico de user, o gateway recusou acesso sem token para rotas protegidas e o pedido criado no order-service foi processado pelo payment-service por meio dos topicos Kafka.",
            style_sheet["Body"],
        ),
        p("Ambiente Validado", style_sheet["Heading1Custom"]),
        key_value_table(
            [
                ("Java", "Amazon Corretto OpenJDK 25.0.3 LTS"),
                ("Maven", "Apache Maven 3.9.16"),
                ("Docker Engine", "28.4.0"),
                ("Gateway", "http://localhost:9090"),
                ("Eureka", "http://localhost:8761"),
            ],
            style_sheet,
        ),
        p("Containers em Execucao", style_sheet["Heading1Custom"]),
        p("Snapshot final de docker compose ps: todos os servicos principais estavam com status healthy.", style_sheet["Body"]),
        matrix(
            ["Servico", "Porta", "Status"],
            [
                ("eureka-service", "8761", "healthy"),
                ("gateway-service", "9090", "healthy"),
                ("user-service", "8080", "healthy"),
                ("order-service", "8081", "healthy"),
                ("payment-service", "8082", "healthy"),
                ("kafka", "29092 -> 9092", "healthy"),
                ("user-postgres", "5432", "healthy"),
                ("order-postgres", "5433 -> 5432", "healthy"),
                ("payment-postgres", "5434 -> 5432", "healthy"),
            ],
            [1.55 * inch, 1.45 * inch, 3.5 * inch],
            style_sheet,
        ),
        p("Testes Automatizados", style_sheet["Heading1Custom"]),
        matrix(
            ["Modulo", "Comando", "Resultado"],
            [
                ("eureka", "mvn test", "BUILD SUCCESS; 1 teste, 0 falhas."),
                ("gateway", "mvn test", "BUILD SUCCESS; 6 testes, 0 falhas."),
                ("user", "mvn test", "BUILD SUCCESS; 28 testes, 0 falhas."),
                ("order", "mvn test", "BUILD SUCCESS; 2 testes, 0 falhas."),
                ("payment", "mvn test", "BUILD SUCCESS; 2 testes, 0 falhas."),
            ],
            [1.15 * inch, 1.3 * inch, 4.05 * inch],
            style_sheet,
        ),
        p("Validacao Funcional Ponta a Ponta", style_sheet["Heading1Custom"]),
        matrix(
            ["Passo", "Evidencia", "Resultado"],
            [
                ("Login", "POST /api/v1/auth/login com admin/Admin@123 retornou token JWT de 179 caracteres.", "OK"),
                ("Seguranca", "GET /api/v1/orders sem Authorization retornou HTTP 401.", "OK"),
                ("Criacao", "POST /api/v1/orders retornou orderId ea8d12ff-31a1-463d-a18e-f6908c068d63 e status PENDING_PAYMENT.", "OK"),
                ("Processamento", "Polling em GET /api/v1/orders/{orderId} confirmou status final PAID.", "OK"),
                ("Persistencia", "order-postgres confirmou order_id ea8d12ff-31a1-463d-a18e-f6908c068d63, status PAID e total 101.70.", "OK"),
            ],
            [1.25 * inch, 4.2 * inch, 1.05 * inch],
            style_sheet,
        ),
        p("Kafka e Resiliencia", style_sheet["Heading1Custom"]),
        p("Os topicos Kafka encontrados confirmam o caminho principal e a infraestrutura de retry/DLT para processamento de pagamentos.", style_sheet["Body"]),
        matrix(
            ["Topico", "Uso observado"],
            [
                ("payment-requests", "Entrada assincrona dos pedidos criados pelo order-service."),
                ("payment-results", "Saida de resultado consumida pelo order-service para atualizar o pedido."),
                ("payment-requests-retry-0", "Primeira etapa de retry configurada no processamento de pagamentos."),
                ("payment-requests-retry-1", "Segunda etapa de retry configurada no processamento de pagamentos."),
                ("payment-requests.DLT", "Destino final para mensagens nao processadas apos tentativas."),
                ("__consumer_offsets", "Topico interno Kafka para controle de offsets dos consumidores."),
            ],
            [2.1 * inch, 4.4 * inch],
            style_sheet,
        ),
        p("Observacoes", style_sheet["Heading1Custom"]),
        matrix(
            ["Item", "Observacao"],
            [
                ("Avisos Maven", "Foram observados warnings de Mockito/ByteBuddy com JDK 25 e avisos de APIs depreciadas em dependencias; nao bloquearam os testes."),
                ("Eureka em testes", "Com o ambiente Docker ativo, o gateway conseguiu consultar o Eureka durante o teste de contexto."),
                ("Estado final", "Os containers permaneceram em execucao para inspecao manual apos a validacao."),
            ],
            [1.55 * inch, 4.95 * inch],
            style_sheet,
        ),
        p("Conclusao", style_sheet["Heading1Custom"]),
        p(
            "O projeto foi executado com sucesso localmente. A autenticacao centralizada no gateway, a descoberta via Eureka, a criacao de pedidos, o processamento assincrono de pagamentos por Kafka, os topicos de resiliencia e a atualizacao persistida do pedido foram confirmados em ambiente Docker Compose.",
            style_sheet["Body"],
        ),
    ]

    doc.build(story, onFirstPage=header_footer, onLaterPages=header_footer)
    print(PDF_PATH)


if __name__ == "__main__":
    build()
