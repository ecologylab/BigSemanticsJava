$(document).ready(
		function() {
			$('tr.nested').children('td.nested_field_value').children('table')
					.hide();
			$('input.composite').parent().parent().parent().children(
					'td.nested_field_value').children('table').find(
					'tr:not(:first)').hide();
			$("input.general").click(
					function(event) {
						$(this).parent().parent().parent().children(
								'td.nested_field_value').children(
								'span.composite_as_scalar').slideToggle();
						$(this).parent().parent().parent().children(
								'td.nested_field_value').children('table')
								.slideToggle();
					});
			$('input.composite').click(
					function(event) {
						$(this).parent().parent().parent().children(
								'td.nested_field_value').children('table')
								.find('tr:not(:first)').slideToggle();
					});
		})