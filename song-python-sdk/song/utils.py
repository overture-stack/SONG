
def convert_to_url_param_list(delimeter='=', **kwargs):
    param_list = []
    for k,v in kwargs.items():
        param_list.append(k+delimeter+v)
    return param_list
